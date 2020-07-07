package com.dkm.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Configuration
public class RabbitMqConfig {

   @Bean
   public Exchange fanoutExchange () {
      return new FanoutExchange("chat_msg_fanoutExchange",false,false);
   }

   @Bean
   public Queue getQueue () {
      return new Queue("chat_msg_chat_queue", false);
   }

   @Bean
   public Queue getNotQueue () {
      return new Queue("chat_msg_not_online_queue",false);
   }

   @Bean
   public Queue getNotManyQueue () {
      return new Queue("chat_ontManyChat_info", false);
   }
}
