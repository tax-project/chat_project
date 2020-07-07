package com.dkm.websocket.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author qf
 * @date 2020/6/8
 * @vesion 1.0
 **/
@Component
public class OtherMqConfig {


   @Bean
   public Queue getWebQueue () {
      return new Queue("chat_msg_chat_queue",false);
   }

   @Bean
   public Queue getNotOnlineQueue () {
      return new Queue("chat_msg_not_online_queue",false);
   }

   @Bean
   public Queue getNotManyQueue () {
      return new Queue("chat_ontManyChat_info", false);
   }
}
