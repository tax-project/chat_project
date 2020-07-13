package com.dkm.friend.service;

import com.dkm.friend.entity.vo.FriendRequestInfoVo;
import com.dkm.friend.entity.vo.FriendRequestVo;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
public interface IFriendRequestService {

   /**
    *  添加好友
    * @param vo
    */
   void friendRequest (FriendRequestVo vo);

   /**
    * 查询所有加我的申请
    * @return   f
    */
   List<FriendRequestInfoVo> listAllRequestFriend ();

   /**
    *  同意或者拒绝好友
    * @param id 申请表id
    * @param fromId 谁加的我的id
    * @param type 0-同意  1-拒绝
    * @param requestRemark 备注
    */
   void operationFriendRequest (Long id, Long fromId, Integer type, String requestRemark);

   /**
    * 删除之前申请好友的信息
    * @param fromId 删人的
    * @param toId 被删的
    */
   void deleteRequestInfo (Long fromId, Long toId);
}
