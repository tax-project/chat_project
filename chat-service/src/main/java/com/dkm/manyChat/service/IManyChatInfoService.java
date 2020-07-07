package com.dkm.manyChat.service;

import com.dkm.manyChat.entity.ManyChatInfo;
import com.dkm.manyChat.entity.vo.ManyChatInfoVo;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/15
 * @vesion 1.0
 **/
public interface IManyChatInfoService {

   /**
    *  建立群聊的具体群员
    * @param list 成员信息
    */
   void insertAllUser(List<ManyChatInfoVo> list);

   /**
    *  查询该群所有的用户信息
    * @param manyChatId 群聊id
    * @return 所有用户id
    */
   List<ManyChatInfo> getManyChatInfoList(Long manyChatId);


}
