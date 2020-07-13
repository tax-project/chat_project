package com.dkm.friend.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author qf
 * @date 2020/5/12
 * @vesion 1.0
 **/
@Data
public class FriendVo implements Serializable {

   /**
    * 谁的账号
    */
   private Long fromId;

   /**
    * 好友的用户id
    */
   private Long toId;

   /**
    * 备注
    */
   private String requestRemark;
}
