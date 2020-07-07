package com.dkm.manyChat.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qf
 * @date 2020/7/7
 * @vesion 1.0
 **/
@Data
public class ManyChatListVo implements Serializable {


   private Long id;

   /**
    * 群头像
    */
   private String headUrl;

   /**
    * 群聊名字
    */
   private String manyName;

   /**
    * 群备注
    */
   private String manyRemark;
}
