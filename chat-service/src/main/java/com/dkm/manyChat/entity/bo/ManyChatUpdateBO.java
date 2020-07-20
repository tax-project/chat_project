package com.dkm.manyChat.entity.bo;

import lombok.Data;

/**
 * @author qf
 * @date 2020/7/17
 * @vesion 1.0
 **/
@Data
public class ManyChatUpdateBO {

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

   /**
    * 公告
    */
   private String manyNotice;
}
