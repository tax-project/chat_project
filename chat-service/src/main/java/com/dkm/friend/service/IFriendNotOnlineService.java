package com.dkm.friend.service;

import com.dkm.friend.entity.FriendNotOnline;
import com.dkm.friend.entity.vo.FriendNotOnlineVo;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
public interface IFriendNotOnlineService {

   /**
    * 查询数据数据
    * @param userId
    * @return
    */
   List<FriendNotOnlineVo> queryOne (Long userId);


   /**
    * 增加未在线消息
    * @param vo
    */
   void insertNotOnline (FriendNotOnlineVo vo);

   /**
    *  更改未读状态
    * @param list id集合
    */
   void deleteLook (List<Long> list);

   /**
    *  批量增加未在线消息
    * @param list 未在线消息参数
    */
   void allInsertNotOnlineInfo (List<FriendNotOnline> list);

   /**
    * 删除所有已读的消息
    */
   void deleteLook ();
}
