package com.dkm.manyChat.entity.bo;

import lombok.Data;

/**
 * @author qf
 * @date 2020/7/17
 * @vesion 1.0
 **/
@Data
public class ManyChatInfoBO {

   /**
    * 用户id
    */
   private Long userId;

   /**
    * 头像地址
    */
   private String headUrl;

   /**
    * 昵称
    */
   private String nickName;

   /**
    * 角色等级
    * 0--群主
    * 1--管理员
    * 2--普通群员
    */
   private Integer manyRoleStatus;

   /**
    * 每个用户在群里的备注
    */
   private String manyNickRemark;
}
