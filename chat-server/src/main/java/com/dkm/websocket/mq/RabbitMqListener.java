package com.dkm.websocket.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dkm.constanct.CodeType;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.exception.ApplicationException;
import com.dkm.utils.StringUtils;
import com.dkm.websocket.entity.SendMsg;
import com.dkm.websocket.utils.ChannelManyGroups;
import com.dkm.websocket.utils.GroupUtils;
import com.dkm.websocket.utils.RSAUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qf
 * @date 2020/3/31
 * @vesion 1.0
 **/
@Component
@Slf4j
@RabbitListener(queues = "chat_msg_queue_")
public class RabbitMqListener {

   @Autowired
   private RedisTemplate redisTemplate;

   @Autowired
   private GroupUtils groupUtils;

   @Autowired
   private RabbitTemplate rabbitTemplate;

   @Autowired
   private ChannelManyGroups channelManyGroups;

   @Value("${RSA.myPublicKey}")
   private String myPublicKey;

   @Autowired
   private RSAUtils rsaUtils;

   @RabbitHandler
   public void rabbitHandle (String msg, com.rabbitmq.client.Channel mqChannel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

      MsgInfo msgInfo = null;
      try {
         msgInfo = JSONObject.parseObject(msg, MsgInfo.class);
      } catch (Exception e) {
         log.info("rabbitMq接收业务平台的消息转换有误...");
         throw new ApplicationException(CodeType.SERVICE_ERROR, "rabbitMq接收业务平台的消息转换有误");
      }

      //用户登录的时候将以用户Id为KEY，设备ID为值存入redis中
      if (msgInfo.getType() == 3) {
         //单聊消息
         //从redis中取出设备ID，找到对应的channel通道
         String cid = (String) redisTemplate.opsForValue().get(msgInfo.getToId());
         //找到对应的通道
         if (cid == null || "".equals(cid)) {
            log.error("redis中未找到对应的设备ID");
            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }
            rabbitTemplate.convertAndSend("chat_msg_not_online_queue",msg);
            return;
         }
         Channel channel = groupUtils.getChannel(cid);

         if (channel == null) {
            //对方未在线，应存入rabbitMQ消息队列中，等待客户端的连接再发送消息
            //存入一个新的队列中
            log.info("未找到对应的channel,有可能是对方未在线,将消息通过mq发送存入数据库");
            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }
            rabbitTemplate.convertAndSend("chat_msg_not_online_queue",msg);
         }

         //将消息发送给客户端
         if (channel != null) {
            log.info("发送单聊消息:" + msgInfo);
//            String encrypt = null;
//            try {
//               encrypt = rsaUtils.encrypt(JSON.toJSONString(msgInfo), myPublicKey);
//            } catch (Exception e) {
//               e.printStackTrace();
//            }
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msgInfo)));

            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }
         }

      }


      if (msgInfo.getType() == 4) {
         //群聊消息
         //去redis中取出所有设备ID，找到channel通道的集合
         List<Channel> channels = new ArrayList<>();
         //去redis中找设备id
         for (Long id : msgInfo.getToIdList()) {
            String cid = (String) redisTemplate.opsForValue().get(id);
            //将除了自己以外的所有群聊人员都发消息
            if (StringUtils.isNotBlank(cid) && !cid.equals(msgInfo.getCid())) {
               Channel channel = groupUtils.getChannel(cid);
               if (channel != null) {
                  channels.add(channel);
               }
            }
         }

         //将建立群聊的channel管理起来
         channelManyGroups.addList(channels);

         SendMsg sendMsg = new SendMsg();
         BeanUtils.copyProperties(msgInfo,sendMsg);

         //将消息群发
         log.info("消息群发:" +sendMsg);
//         String encrypt = null;
//         try {
//            encrypt = rsaUtils.encrypt(JSON.toJSONString(msgInfo), myPublicKey);
//         } catch (Exception e) {
//            e.printStackTrace();
//         }
         channelManyGroups.broadcast(new TextWebSocketFrame(JSON.toJSONString(sendMsg)));


         try {
            mqChannel.basicAck(deliveryTag,true);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }


      if (msgInfo.getType() == 100) {
         //强制下线

         if (msgInfo.getCid() != null) {
            //在线状态
            msgInfo.setMsg("您的账号在其他地点登录，您被强制下线.");

            Channel channel = groupUtils.getChannel(msgInfo.getCid());

            //将消息发送给客户端
            if (channel != null) {
               log.info("挤下线:" + msgInfo);
//               String encrypt = null;
//               try {
//                  encrypt = rsaUtils.encrypt(JSON.toJSONString(msgInfo), myPublicKey);
//               } catch (Exception e) {
//                  e.printStackTrace();
//               }
               channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msgInfo)));

               if (msgInfo.getCid() != null) {
                  //移除连接
                  groupUtils.removeById(msgInfo.getCid());

               }

               try {
                  mqChannel.basicAck(deliveryTag,true);
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }


      }


      //用户登录的时候将以用户Id为KEY，设备ID为值存入redis中
      if (msgInfo.getType() == 101) {
         //添加好友提醒
         //从redis中取出设备ID，找到对应的channel通道
         String cid = (String) redisTemplate.opsForValue().get(msgInfo.getToId());
         //找到对应的通道
         if (cid == null || "".equals(cid)) {
            log.error("redis中未找到对应的设备ID");
            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }
            return;
         }
         Channel channel = groupUtils.getChannel(cid);

         if (channel == null) {
            //对方未在线，应存入rabbitMQ消息队列中，等待客户端的连接再发送消息
            //存入一个新的队列中
            log.info("未找到对应的channel,有可能是对方未在线,将消息通过mq发送存入数据库");
            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }
            rabbitTemplate.convertAndSend("chat_msg_not_online_queue",msg);
         }

         //将消息发送给客户端
         if (channel != null) {
            log.info("发送加好友消息:" + msgInfo);
//            String encrypt = null;
//            try {
//               encrypt = rsaUtils.encrypt(JSON.toJSONString(msgInfo), myPublicKey);
//            } catch (Exception e) {
//               e.printStackTrace();
//            }
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msgInfo)));

            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }
         }

      }


      if (msgInfo.getType() == 102) {
         //退出登录

         if (msgInfo.getCid() != null) {
            //移除连接
            groupUtils.removeById(msgInfo.getCid());

            log.info("退出登录--->channel:" + groupUtils.getChannel(msgInfo.getCid()));

            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }

         }


      }


      if (msgInfo.getType() == 103) {
         //取消前端加好友的红点

         if (msgInfo.getCid() != null) {

            Channel channel = groupUtils.getChannel(msgInfo.getCid());
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msgInfo)));

            log.info("取消前端加好友的红点--->" + msgInfo);

            try {
               mqChannel.basicAck(deliveryTag,true);
            } catch (IOException e) {
               e.printStackTrace();
            }

         }


      }


   }
}
