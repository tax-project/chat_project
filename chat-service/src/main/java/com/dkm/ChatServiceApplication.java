package com.dkm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author qf
 */
@EnableSwagger2
@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.dkm.*.dao")
@EnableCaching
public class ChatServiceApplication extends SpringBootServletInitializer {

   public static void main(String[] args) {
      SpringApplication.run(ChatServiceApplication.class, args);
   }

   /**
    * 打包
    * @param builder
    * @return
    */
   @Override
   protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      return builder.sources(ChatServiceApplication.class);
   }


}
