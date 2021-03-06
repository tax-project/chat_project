package com.dkm.websocket.handle;


import com.dkm.constanct.CodeType;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.exception.ApplicationException;
import com.dkm.websocket.utils.GroupUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author qf
 * @date 2020/4/2
 * @vesion 1.0
 **/
@Component
@Slf4j
@ChannelHandler.Sharable
public class ConnectWebSocketHandle extends SimpleChannelInboundHandler<MsgInfo> {

   @Autowired
   private GroupUtils groupUtils;

   /**
    * 一个连接上线
    * @param ctx
    * @throws Exception
    */
   @Override
   public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
      log.info("有一个新的连接上线");
   }


   /**
    * 一个连接下线
    * @param ctx
    * @throws Exception
    */
   @Override
   public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
      log.info("有一个连接下线");
      groupUtils.removeByChannel(ctx.channel());
   }


   @Override
   protected void channelRead0(ChannelHandlerContext ctx, MsgInfo msgInfo) throws Exception {

      //设备id
      String cid = msgInfo.getCid();

      if (msgInfo.getType() == 1) {
         //连接请求
         Channel channel = ctx.channel();

         //将设备Id与连接通道进行绑定
         groupUtils.put(cid,channel);

         log.info("握手成功--当前连接数：" + groupUtils.size());
      } else {
         //先判断有没有通道
         Channel channel = groupUtils.getChannel(cid);

         if (channel != null) {
            //非握手请求，继续往后透传
            ctx.fireChannelRead(msgInfo);
         } else {
            channelUnregistered (ctx);
         }

      }

   }


   /**
    * 异常处理
    * @param ctx
    * @param cause
    * @throws Exception
    */
   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      log.info("连接发生异常,异常是:" +cause);
      groupUtils.removeByChannel(ctx.channel());
   }


}
