package com.dkm.manyChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.manyChat.dao.ManyChatInfoMapper;
import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.ManyChatInfo;
import com.dkm.manyChat.entity.bo.ManyChatInfoBO;
import com.dkm.manyChat.entity.vo.ManyChatInfoVo;
import com.dkm.manyChat.service.IManyChatInfoService;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ManyChatInfoServiceImpl extends ServiceImpl<ManyChatInfoMapper, ManyChatInfo> implements IManyChatInfoService {


   @Autowired
   private LocalUser localUser;

   /**
    *  建立群聊的具体群员
    * @param list 成员信息
    */
   @Override
   public void insertAllUser(List<ManyChatInfoVo> list) {

      Integer integer = baseMapper.insertAllUser(list);

      if (integer <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "建立群聊错误");
      }

   }

   @Override
   public List<ManyChatInfo> getManyChatInfoList(Long manyChatId) {

      LambdaQueryWrapper<ManyChatInfo> wrapper = new LambdaQueryWrapper<ManyChatInfo>()
            .eq(ManyChatInfo::getManyChatId,manyChatId);

      return baseMapper.selectList(wrapper);
   }

   @Override
   public void deleteManyChatInfo(Long userId, Long manyChatId) {
      LambdaQueryWrapper<ManyChatInfo> wrapper = new LambdaQueryWrapper<ManyChatInfo>()
            .eq(ManyChatInfo::getManyChatId, manyChatId)
            .eq(ManyChatInfo::getUserId, userId);

      int delete = baseMapper.delete(wrapper);

      if (delete <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "退出失败");
      }
   }

   @Override
   public List<ManyChatInfoBO> getManyInfoAllList(Long manyChatId) {
      return baseMapper.getManyInfoAllList(manyChatId);
   }

   @Override
   public void updateAdmin(Long userId, Long manyChatId) {
      UserLoginQuery user = localUser.getUser();

      List<ManyChatInfo> list = getManyChatInfoList(manyChatId);

      Integer status = 2;

      List<Integer> integerList = new ArrayList<>();

      for (ManyChatInfo manyChatInfo : list) {
         if (manyChatInfo.getUserId().equals(user.getId())) {
            status = manyChatInfo.getManyRoleStatus();
         }

         if (manyChatInfo.getManyRoleStatus() == 1) {
            integerList.add(1);
         }
      }

      if (status != 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "只有群主才能设置");
      }

      if (integerList.size() >= 5) {
         //每个群不超过5个管理员
         throw new ApplicationException(CodeType.SERVICE_ERROR, "每个群不超过5个管理员");
      }

      LambdaQueryWrapper<ManyChatInfo> wrapper = new LambdaQueryWrapper<ManyChatInfo>()
            .eq(ManyChatInfo::getUserId,userId)
            .eq(ManyChatInfo::getManyChatId,manyChatId);

      ManyChatInfo manyChatInfo = new ManyChatInfo();
      manyChatInfo.setManyRoleStatus(1);
      int update = baseMapper.update(manyChatInfo, wrapper);

      if (update <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "修改失败");
      }
   }
}
