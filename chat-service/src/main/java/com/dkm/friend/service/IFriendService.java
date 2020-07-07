package com.dkm.friend.service;

import com.dkm.friend.entity.Friend;
import com.dkm.friend.entity.bo.FriendAllListBo;
import com.dkm.friend.entity.bo.FriendBo;
import com.dkm.friend.entity.vo.FriendAllListVo;
import com.dkm.friend.entity.vo.FriendVo;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
public interface IFriendService {

   /**
    * 成为好友
    * @param vo
    */
   void insertFriend (FriendVo vo);

   /**
    *  删除好友
    * @param fromId 自己的id
    * @param toId 要删除的人的id
    */
   void deleteFriend (Long fromId,Long toId);

   /**
    *  展示全部好友
    * @param userId 用户id
    * @return
    */
   List<FriendAllListVo> listAllFriend (Long userId);


   /**
    *  根据用户Id搜索用户信息以及好友情况
    * @param id 用户Id
    * @return 用户以及好友信息
    */
   FriendBo querySolrInfo (Long id);

   /**
    *  修改对好友的备注
    * @param remark 好友备注
    * @param id  好友的id
    * @param fromId 自己的用户id
    */
   void updateFriendRemark (String remark, Long fromId, Long id);

   /**
    *  查询一条数据
    * @param fromId 谁
    * @param toId 谁的好友
    * @return 好友记录
    */
   Friend queryOne (Long fromId, Long toId);

}
