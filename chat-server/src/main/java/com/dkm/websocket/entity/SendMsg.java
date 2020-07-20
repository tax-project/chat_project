package com.dkm.websocket.entity;

import lombok.Data;

/**
 * @author qf
 * @date 2020/6/28
 * @vesion 1.0
 **/
@Data
public class SendMsg {

   /**
    * 消息内容
    */
   private String msg;

   /**
    * 谁发的信息
    */
   private Long fromId;

   /**
    * 发送人名字
    */
   private String fromName;

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
    */
   private Integer type;

   /**
    * 群聊id
    */
   private Long manyChatId;

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

   /**
    *  语音发送时长
    */
   private String sendTime;
}
