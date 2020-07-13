package com.dkm.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author qf
 * @date 2020/7/8
 * @vesion 1.0
 **/
@Data
public class MsgFriends {

   /**
    * 消息内容
    */
   private String msg;

   /**
    * 谁发的信息
    */
   private Long fromId;

   /**
    * 发给谁
    */
   private Long toId;

   /**
    * 1--连接请求
    * 2--心跳消息
    * 3--单聊消息
    * 4--群聊信息
    *
    *
    *
    * 100--强制下线
    * 101-申请添加好友
    * 102-通知退出登录  移除连接
    * 103-取消前端小红点
    * 104-建立群聊通知
    * 105-成为好友通知
    */
   private Integer type;

   /**
    * 设备id
    */
   private String cid;

   /**
    * 1--文本
    * 2--图片
    * 3--音频
    */
   private Integer msgType;

   /**
    * 发送时间
    */
   private String sendDate;
}
