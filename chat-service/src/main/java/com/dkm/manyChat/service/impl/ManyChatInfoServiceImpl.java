package com.dkm.manyChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.manyChat.dao.ManyChatInfoMapper;
import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.ManyChatInfo;
import com.dkm.manyChat.entity.vo.ManyChatInfoVo;
import com.dkm.manyChat.service.IManyChatInfoService;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
