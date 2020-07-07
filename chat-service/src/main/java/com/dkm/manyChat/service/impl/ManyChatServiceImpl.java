package com.dkm.manyChat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dkm.config.RedisConfig;
import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.file.service.IFileService;
import com.dkm.file.utils.FileVo;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.manyChat.dao.ManyChatMapper;
import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.vo.ManyChatInfoVo;
import com.dkm.manyChat.entity.vo.ManyChatListVo;
import com.dkm.manyChat.entity.vo.ManyChatVo;
import com.dkm.manyChat.service.IManyChatInfoService;
import com.dkm.manyChat.service.IManyChatService;
import com.dkm.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
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

   @Override
   @CacheEvict(value = "manyChat", key = "'manyChat' + #result")
   public Long insertManyChat(ManyChatVo vo) {
      ManyChat manyChat = new ManyChat();

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

      for (Long userId : vo.getList()) {
         ManyChatInfoVo infoVo = new ManyChatInfoVo();
         infoVo.setId(idGenerator.getNumberId());
         infoVo.setManyChatId(manyChatId);
         infoVo.setUserId(userId);
         infoVo.setRoleStatus(2);
         list.add(infoVo);
      }

      UserLoginQuery user = localUser.getUser();

      ManyChatInfoVo infoVo = new ManyChatInfoVo();
      infoVo.setId(idGenerator.getNumberId());
      infoVo.setManyChatId(manyChatId);
      infoVo.setUserId(user.getId());
      infoVo.setRoleStatus(0);
      list.add(infoVo);

      manyChatInfoService.insertAllUser(list);

      return user.getId();
   }

   @Override
   @Cacheable(value = "manyChatInfo", key = "'manyChatInfo' + #id")
   public ManyChat queryById(Long id) {
      return baseMapper.selectById(id);
   }

   @Override
   @Cacheable(value = "manyChat", key = "'manyChat' + #userId")
   public List<ManyChatListVo> queryManyChatList(Long userId) {
      //查询我的群聊
      return baseMapper.queryManyChatList(userId);
   }


}
