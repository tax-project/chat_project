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
import com.dkm.manyChat.entity.bo.ManyChatBo;
import com.dkm.manyChat.entity.vo.ManyChatInfoVo;
import com.dkm.manyChat.entity.vo.ManyChatListVo;
import com.dkm.manyChat.entity.vo.ManyChatVo;
import com.dkm.manyChat.service.IManyChatInfoService;
import com.dkm.manyChat.service.IManyChatService;
import com.dkm.utils.IdGenerator;
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
import java.util.List;

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

      UserLoginQuery user = localUser.getUser();

      ManyChatInfoVo infoVo = new ManyChatInfoVo();
      infoVo.setId(idGenerator.getNumberId());
      infoVo.setManyChatId(manyChatId);
      infoVo.setUserId(user.getId());
      infoVo.setRoleStatus(0);
      list.add(infoVo);

      manyChatInfoService.insertAllUser(list);

     //添加自己
      longList.add(local.getId());

      //通知客户端收到好友申请的通知
      MsgInfo msgInfo = new MsgInfo();
      msgInfo.setType(104);
      msgInfo.setFromId(user.getId());
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
   @Cacheable(value = "manyChatInfo", key = "'manyChatInfo' + #id")
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

      List<ManyChatInfoVo> list = new ArrayList<>();

      for (Long userId : bo.getList()) {
         ManyChatInfoVo infoVo = new ManyChatInfoVo();
         infoVo.setId(idGenerator.getNumberId());
         infoVo.setManyChatId(bo.getManyChatId());
         infoVo.setUserId(userId);
         infoVo.setRoleStatus(2);
         list.add(infoVo);
      }

      manyChatInfoService.insertAllUser(list);
   }

   @Override
   public void exitManyChat(Long manyChatId) {

      UserLoginQuery user = localUser.getUser();

      //退出群聊
      manyChatInfoService.deleteManyChatInfo(user.getId(), manyChatId);
   }


}
