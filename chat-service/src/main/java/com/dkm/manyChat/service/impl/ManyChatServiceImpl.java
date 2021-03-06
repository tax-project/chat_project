package com.dkm.manyChat.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dkm.config.RedisConfig;
import com.dkm.constanct.CodeType;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.exception.ApplicationException;
import com.dkm.file.service.IFileService;
import com.dkm.file.utils.FileVo;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.manyChat.dao.ManyChatMapper;
import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.ManyChatInfo;
import com.dkm.manyChat.entity.bo.ManyChatBo;
import com.dkm.manyChat.entity.bo.ManyChatUpdateBO;
import com.dkm.manyChat.entity.vo.ManyChatInfoVo;
import com.dkm.manyChat.entity.vo.ManyChatListVo;
import com.dkm.manyChat.entity.vo.ManyChatVo;
import com.dkm.manyChat.service.IManyChatInfoService;
import com.dkm.manyChat.service.IManyChatService;
import com.dkm.utils.IdGenerator;
import com.dkm.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qf
 * @date 2020/5/15
 * @vesion 1.0
 **/
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ManyChatServiceImpl extends ServiceImpl<ManyChatMapper, ManyChat> implements IManyChatService {

   @Autowired
   private IdGenerator idGenerator;

   @Value("${file.qrCodeUrl}")
   private String qrCodeUrl;

   @Autowired
   private IFileService fileService;

   @Autowired
   private LocalUser localUser;

   @Autowired
   private IManyChatInfoService manyChatInfoService;

   @Autowired
   private RabbitTemplate rabbitTemplate;

   @Override
   public void insertManyChat(ManyChatVo vo) {
      ManyChat manyChat = new ManyChat();

      UserLoginQuery local = localUser.getUser();

      //群聊id
      Long manyChatId = idGenerator.getNumberId();
      manyChat.setId(manyChatId);

      manyChat.setCreateDate(LocalDateTime.now());
      manyChat.setManyName(vo.getManyName());
      //生成群聊二维码
      FileVo qrCode = fileService.getQrCode(qrCodeUrl + "?manyChatId=" + manyChatId);
      manyChat.setManyQrCode(qrCode.getFileUrl());
      manyChat.setHeadUrl(vo.getManyHeadUrl());

      int insert = baseMapper.insert(manyChat);

      if (insert <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "建立群聊失败");
      }

      List<ManyChatInfoVo> list = new ArrayList<>();
      List<Long> longList = new ArrayList<>();

      for (Long userId : vo.getList()) {

         if (userId == null) {
            throw new ApplicationException(CodeType.PARAMETER_ERROR);
         }

         ManyChatInfoVo infoVo = new ManyChatInfoVo();
         infoVo.setId(idGenerator.getNumberId());
         infoVo.setManyChatId(manyChatId);
         infoVo.setUserId(userId);
         infoVo.setRoleStatus(2);
         list.add(infoVo);

         longList.add(userId);
      }

      ManyChatInfoVo infoVo = new ManyChatInfoVo();
      infoVo.setId(idGenerator.getNumberId());
      infoVo.setManyChatId(manyChatId);
      infoVo.setUserId(local.getId());
      infoVo.setRoleStatus(0);
      list.add(infoVo);

      manyChatInfoService.insertAllUser(list);

     //添加自己
      longList.add(local.getId());

      //通知客户端收到好友申请的通知
      MsgInfo msgInfo = new MsgInfo();
      msgInfo.setType(104);
      msgInfo.setFromId(local.getId());
      msgInfo.setToIdList(longList);
      msgInfo.setMsg("成功建立群聊,快聊天吧~");
      msgInfo.setCid(null);
      msgInfo.setSendDate(null);
      msgInfo.setIsFriend(0);
      msgInfo.setSendTime(null);
      msgInfo.setMsgType(1);
      msgInfo.setManyChatId(manyChatId);

      //将好友申请同步发送给好友
      rabbitTemplate.convertAndSend("chat_msg_fanoutExchange","", JSON.toJSONString(msgInfo));
   }

   @Override
   public ManyChat queryById(Long id) {
      return baseMapper.selectById(id);
   }

   @Override
   public List<ManyChatListVo> queryManyChatList(Long userId) {
      //查询我的群聊
      return baseMapper.queryManyChatList(userId);
   }

   /**
    *  拉人进群
    * @param bo 人员参数
    */
   @Override
   public void addManyChat(ManyChatBo bo) {

      UserLoginQuery user = localUser.getUser();
      MsgInfo msgInfo = new MsgInfo();

      List<ManyChatInfo> infoList = manyChatInfoService.getManyChatInfoList(bo.getManyChatId());

      Set<Long> set = new HashSet<>();
      for (ManyChatInfo manyChatInfo : infoList) {
         for (Long userId : bo.getList()) {
            if (!manyChatInfo.getUserId().equals(userId)) {
               set.add(userId);
            }
         }
      }

      List<ManyChatInfoVo> list = new ArrayList<>();

      for (Long userId : set) {
         ManyChatInfoVo infoVo = new ManyChatInfoVo();
         infoVo.setId(idGenerator.getNumberId());
         infoVo.setManyChatId(bo.getManyChatId());
         infoVo.setUserId(userId);
         infoVo.setRoleStatus(2);
         list.add(infoVo);
      }

      manyChatInfoService.insertAllUser(list);

      //添加通知
      msgInfo.setType(104);
      msgInfo.setFromId(user.getId());
      msgInfo.setToIdList(bo.getList());
      msgInfo.setMsg("加入了群聊");
      msgInfo.setCid(null);
      msgInfo.setSendDate(null);
      msgInfo.setIsFriend(0);
      msgInfo.setSendTime(null);
      msgInfo.setMsgType(1);
      msgInfo.setManyChatId(bo.getManyChatId());

      //将好友申请同步发送给好友
      rabbitTemplate.convertAndSend("chat_msg_fanoutExchange","", JSON.toJSONString(msgInfo));
   }

   @Override
   public void exitManyChat(Long manyChatId) {

      UserLoginQuery user = localUser.getUser();

      //退出群聊
      manyChatInfoService.deleteManyChatInfo(user.getId(), manyChatId);
   }

   @Override
   public void updateManyChat(ManyChatUpdateBO manyChatUpdateBO) {

      UserLoginQuery user = localUser.getUser();
      //查询群聊具体人员是否是管理员
      List<ManyChatInfo> list = manyChatInfoService.getManyChatInfoList(manyChatUpdateBO.getId());

      Integer status = 2;

      List<Long> longList = new ArrayList<>();

      for (ManyChatInfo manyChatInfo : list) {
         if (manyChatInfo.getUserId().equals(user.getId())) {
            status = manyChatInfo.getManyRoleStatus();
         }
         longList.add(manyChatInfo.getUserId());
      }

      ManyChat manyChat = new ManyChat();

      manyChat.setId(manyChatUpdateBO.getId());

      String msg = null;

      if (StringUtils.isNotBlank((manyChatUpdateBO.getHeadUrl()))) {
         if (status == 2) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "您不是管理员或群主不能修改");
         }
         manyChat.setHeadUrl(manyChatUpdateBO.getHeadUrl());
         msg = "修改了群头像";
      }

      if (StringUtils.isNotBlank(manyChatUpdateBO.getManyName())) {
         manyChat.setManyName(manyChatUpdateBO.getManyName());
         msg = "修改了群名字";
      }

      if (StringUtils.isNotBlank(manyChatUpdateBO.getManyNotice())) {
         if (status == 2) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "您不是管理员或群主不能修改");
         }
         manyChat.setManyNotice(manyChatUpdateBO.getManyNotice());
         msg = "修改了公告";
      }

      if (StringUtils.isNotBlank(manyChatUpdateBO.getManyRemark())) {
         if (status == 2) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "您不是管理员或群主不能修改");
         }
         manyChat.setManyRemark(manyChatUpdateBO.getManyRemark());
         msg = "修改了群备注";
      }

      int updateById = baseMapper.updateById(manyChat);

      if (updateById <= 0) {
         log.info("update manyChat fail.");
         throw new ApplicationException(CodeType.SERVICE_ERROR);
      }

      MsgInfo msgInfo = new MsgInfo();

      //添加通知
      msgInfo.setType(4);
      msgInfo.setFromId(user.getId());
      msgInfo.setToIdList(longList);
      msgInfo.setMsg(msg);
      msgInfo.setCid(null);
      msgInfo.setSendDate(null);
      msgInfo.setIsFriend(0);
      msgInfo.setSendTime(null);
      msgInfo.setMsgType(1);
      msgInfo.setManyChatId(manyChatUpdateBO.getId());

      //将好友申请同步发送给好友
      rabbitTemplate.convertAndSend("chat_msg_fanoutExchange","", JSON.toJSONString(msgInfo));
   }


}
