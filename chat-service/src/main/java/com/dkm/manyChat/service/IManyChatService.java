package com.dkm.manyChat.service;

import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.bo.ManyChatBo;
import com.dkm.manyChat.entity.bo.ManyChatUpdateBO;
import com.dkm.manyChat.entity.vo.ManyChatListVo;
import com.dkm.manyChat.entity.vo.ManyChatVo;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/15
 * @vesion 1.0
 **/
public interface IManyChatService {

   /**
    *  建立群聊
    * @param vo 建立群聊信息
    */
   void insertManyChat(ManyChatVo vo);

   /**
    *  根据id查询群聊信息
    * @param id 群聊Id
    * @return 返回群聊的资料
    */
   ManyChat queryById (Long id);

   /**
    *  查询我的群聊
    * @return 返回我的群聊列表
    * @param userId 用户Id
    */
   List<ManyChatListVo> queryManyChatList (Long userId);

   /**
    *  添加人进群
    * @param bo 人员参数
    */
   void addManyChat (ManyChatBo bo);

   /**
    *  退出群聊
    * @param manyChatId 群聊id
    */
   void exitManyChat (Long manyChatId);

   /**
    *  修改群资料
    * @param manyChatUpdateBO 要修改的群信息
    */
   void updateManyChat (ManyChatUpdateBO manyChatUpdateBO);
}
