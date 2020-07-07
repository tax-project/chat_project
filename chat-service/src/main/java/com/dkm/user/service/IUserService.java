package com.dkm.user.service;

import com.dkm.entity.websocket.MsgInfo;
import com.dkm.friend.entity.vo.FriendAllListVo;
import com.dkm.friend.entity.vo.IdVo;
import com.dkm.user.entity.User;
import com.dkm.user.entity.vo.UpdateVo;
import com.dkm.user.entity.vo.UserUpdateInfoVo;
import com.dkm.user.entity.vo.UserVo;

import java.util.List;
import java.util.Map;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
public interface IUserService {

   /**
    * 注册用户
    * @param userVo
    */
   void registerUser (UserVo userVo);


   /**
    * 登录
    * @param userName
    * @param password
    * @return
    */
   Map<String,Object> loginUser (String userName, String password);

   /**
    * 修改信息
    * @param vo
    */
   void updateUserInfo (UserUpdateInfoVo vo);

   /**
    * 修改密码
    * @param vo
    */
   void updatePassword (UpdateVo vo);

   /**
    * 根据id查询用户详细信息
    * @param id 用户Id
    * @return
    */
   User queryUserById (Long id);

   /**
    *  查询当前登陆人所有好友
    * @param list id的集合
    * @return 好友信息的集合
    */
   List<FriendAllListVo> queryAllFriend (List<IdVo> list);

   /**
    *  修改头像
    * @param heardUrl 头像地址
    * @return 用户id
    */
   Long updateHeardUrl (String heardUrl);

   /**
    *  退出登录
    */
   void signOutLogin ();

   /**
    *  根据账号查询
    * @param userName
    * @return
    */
   User queryUserByName (String userName);

   /**
    *  上线触发离线消息
    */
   List<MsgInfo> getNotOnlineInfo ();

}
