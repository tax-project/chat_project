package com.dkm.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dkm.config.RedisConfig;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.friend.entity.Friend;
import com.dkm.friend.service.IFriendService;
import com.dkm.listener.vo.ChatVo;
import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.ManyChatInfo;
import com.dkm.manyChat.service.IManyChatInfoService;
import com.dkm.manyChat.service.IManyChatService;
import com.dkm.user.entity.User;
import com.dkm.user.service.IUserService;
import com.dkm.utils.DateUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Slf4j
@Component
@RabbitListener(queues = "chat_msg_chat_queue")
public class MqListener {

   @Autowired
   private RabbitTemplate rabbitTemplate;

   @Autowired
   private IManyChatInfoService manyChatInfoService;

   @Autowired
   private IFriendService friendService;

   @RabbitHandler
   public void msg (@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, String msgInfo, Channel channel) {
      log.info("通讯平台收到服务器的消息:" +msgInfo);

      //一些业务操作之后
      //判断是不是群聊消息
      MsgInfo info = null;
      try {
         info = JSONObject.parseObject(msgInfo, MsgInfo.class);
      } catch (Exception e) {
         log.info("rabbitMq接收业务平台的消息转换有误...");
         e.printStackTrace();
      }

      if (info.getType() == 3) {
         //单聊消息
         info.setIsFriend(0);
         Friend friend = friendService.queryOne(info.getFromId(), info.getToId());

         if (friend == null) {
            info.setType(105);
            //拦截返回不是好友的消息
            info.setIsFriend(1);
         }
      }

      if (info.getType() == 4) {
         //群聊消息
         List<Long> longList = new ArrayList<>();
         if (info.getManyChatId() != null) {
            List<ManyChatInfo> list = manyChatInfoService.getManyChatInfoList(info.getManyChatId());
            for (ManyChatInfo chatInfo : list) {
               longList.add(chatInfo.getUserId());
            }
            info.setToIdList(longList);
         }
      }

      //将消息传给服务器
      //此处也要重新申明交换机，不然生产者找不到交换机
      //广播形式发给所有服务器
      rabbitTemplate.convertAndSend("chat_msg_fanoutExchange","",JSON.toJSONString(info));

      //确认消息
      try {
         channel.basicAck(deliveryTag,true);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
