package com.dkm.websocket.utils;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qf
 * @date 2020/3/11
 * @vesion 1.0
 **/
@Component
public class GroupUtils {

   Map<String, Channel> map = new ConcurrentHashMap<>();

   /**
    * 将id和channel管理
    * @param cid
    * @param channel
    */
   public void put (String cid, Channel channel) {
      map.put(cid, channel);
   }

   /**
    * 通过id得到通道
    * @param cid
    * @return
    */
   public Channel getChannel (String cid) {
      return map.get(cid);
   }

   /**
    * 通过id移除通道
    * @param cid
    */
   public Channel removeById (String cid) {
      return map.remove(cid);
   }

   /**
    * 查询当前连接的长度
    * @return
    */
   public int size () {
      return map.size();
   }

   /**
    * 移除所有连接
    * @param channel
    */
   public void removeByChannel (Channel channel) {

      Set<Map.Entry<String, Channel>> entries = map.entrySet();

      for (Map.Entry<String, Channel> entry : new HashSet<>(entries)) {
         if(entry.getValue() == channel){
            map.remove(entry.getKey());
         }
      }

   }
}
