package com.dkm.config;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author qf
 * @date 2020/4/24
 * @vesion 1.0
 **/
@Configuration
public class RedisConfig {

   @Autowired
   private RedisTemplate redisTemplate;

   /**
    * 将字符串存入redis中
    * @param k
    * @param v
    */
   public void  setString (Object k,Object v) {
      redisTemplate.opsForValue().set(k,v);
   }

   /**
    *  将存入的字符串取出来
    * @param k
    * @return
    */
   public Object getString (Object k) {
      return redisTemplate.opsForValue().get(k);
   }

   /**
    *  将字符串存入redis中并设置过期时间
    *  过期时间单位为秒
    * @param k 键
    * @param v 值
    * @param t 过期时间  单位秒
    */
   public void  setString (Object k, Object v, Long t) {
      redisTemplate.opsForValue().set(k,v,t, TimeUnit.SECONDS);
   }

   /**
    * 移除redis的某个值
    * 根据键
    * @param k
    */
   public void remove (Object k) {
      redisTemplate.delete(k);
   }

   /**
    * 从左边添加元素
    * 先进后出
    * @param k
    * @param list
    */
   public void setLeftList (Object k, List<Object> list) {
      redisTemplate.opsForList().leftPushAll(k,list);
   }

   /**
    * 从右边将集合加入到redis中
    * 先进先出
    * 放入数据与取出来数据顺序一致
    * @param k
    * @param list
    */
   public void setRightList (Object k, List<Object> list) {
      redisTemplate.opsForList().rightPushAll(k,list);
   }

   /**
    * 取出存入集合的所有数据
    * @param k
    * @return
    */
   public List<Object> getList (Object k) {
      return redisTemplate.opsForList().range(k,0,-1);
   }

   /**
    * 获得分布式锁
    * 过期时间  默认5S
    * @param lock
    * @return
    */
   public Boolean redisLock (String lock) {
      RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
      return connection.set(lock.getBytes(),
            lock.getBytes(),
            Expiration.seconds(5),
            RedisStringCommands.SetOption.SET_IF_ABSENT);
//      Boolean aBoolean = connection.setNX(lock.getBytes(), lock.getBytes());
//      connection.expire(lock.getBytes(),10);
//      return aBoolean;
   }

   /**
    * 是否拿到分布式锁
    * @param lock
    * @return
    */
   public Boolean isLock (String lock) {
      RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
      byte[] bytes = connection.get(lock.getBytes());

      if (bytes != null) {
         return true;
      }

      return false;
   }

   /**
    * 删除分布式锁
    * @param lock
    */
   public void deleteLock (String lock) {
      RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
      connection.del(lock.getBytes());
   }

   /**
    * 失效时间
    * @param lock
    * @return
    */
   public Long getTtl (String lock) {
      RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
      return connection.ttl(lock.getBytes());
   }
}
