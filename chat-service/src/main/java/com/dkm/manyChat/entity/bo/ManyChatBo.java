package com.dkm.manyChat.entity.bo;

import lombok.Data;

import java.util.List;

/**
 * @author qf
 * @date 2020/7/8
 * @vesion 1.0
 **/
@Data
public class ManyChatBo {

   /**
    *  群聊id
    */
   private Long manyChatId;

   /**
    * 用户id集合
    */
   private List<Long> list;
}
