package com.dkm.listener;

import com.alibaba.fastjson.JSONObject;
import com.dkm.entity.vo.Msg;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.friend.entity.FriendNotOnline;
import com.dkm.friend.entity.vo.FriendNotOnlineVo;
import com.dkm.friend.service.IFriendNotOnlineService;
import com.dkm.utils.IdGenerator;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qf
 * @date 2020/7/7
 * @vesion 1.0
 **/
@Slf4j
@Component
@RabbitListener(queues = "chat_ontManyChat_info")
public class MqNotManyChatListener {

   @Autowired
   private IdGenerator idGenerator;

   @Autowired
   private IFriendNotOnlineService friendNotOnlineService;

   @RabbitHandler
   public void getNotOnlineMsg (@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, String notOnlineMsg, Channel channel) {

      //业务操作
      //接收到群聊未在线消息
      log.info("--接收到群聊未在线的信息:" +notOnlineMsg);
      Msg msg1 = null;
      try {
         msg1 = JSONObject.parseObject(notOnlineMsg, Msg.class);
      } catch (Exception e) {
         e.printStackTrace();
      }

      //拿到群聊未在线的消息的集合
      List<MsgInfo> list = msg1.getList();
      List<FriendNotOnline> paramList = new ArrayList<>();

      for (MsgInfo msg : list) {
         FriendNotOnline friendNotOnline = new FriendNotOnline();
         friendNotOnline.setId(idGenerator.getNumberId());
         friendNotOnline.setFromId(msg.getFromId());
         friendNotOnline.setToId(msg.getToId());
         friendNotOnline.setContent(msg.getMsg());
         friendNotOnline.setCreateDate(LocalDateTime.now());
         friendNotOnline.setType(msg.getType());
         friendNotOnline.setIsLook(0);

         friendNotOnline.setCid(msg.getCid());
         friendNotOnline.setManyChatId(msg.getManyChatId());
         friendNotOnline.setMsgType(msg.getMsgType());
         friendNotOnline.setSendTime(msg.getSendTime());
         paramList.add(friendNotOnline);
      }

      //确认消息
      try {
         channel.basicAck(deliveryTag,true);
      } catch (IOException e) {
         e.printStackTrace();
      }


      //批量添加进数据库
      friendNotOnlineService.allInsertNotOnlineInfo(paramList);

   }
}
