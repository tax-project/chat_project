package com.dkm.manyChat.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qf
 * @date 2020/5/16
 * @vesion 1.0
 **/
@Data
public class ManyChatInfoVo implements Serializable {

   private Long id;

   /**
    *  用户
    */
   private Long userId;

   /**
    *  0--群主
    *  1--管理员
    *  2--普通用户
    */
   private Integer roleStatus;

   /**
    *  群聊id
    */
   private Long manyChatId;
}
