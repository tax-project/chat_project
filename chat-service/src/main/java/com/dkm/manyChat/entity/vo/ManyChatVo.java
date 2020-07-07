package com.dkm.manyChat.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author qf
 * @date 2020/5/16
 * @vesion 1.0
 **/
@Data
public class ManyChatVo implements Serializable {

   /**
    * 群聊名字
    */
   private String manyName;

   /**
    * 群聊头像
    */
   private String manyHeadUrl;

   /**
    * 用户id集合
    */
   private List<Long> list;
}
