package com.dkm.jwt.contain;

import com.dkm.jwt.entity.UserLoginQuery;

/**
 * @author qf
 * @date 2020/8/27
 * @vesion 1.0
 **/
public class ThreadLocalMap {

   private static ThreadLocal<UserLoginQuery> threadLocal = new ThreadLocal<>();

   public static UserLoginQuery get () {
      return threadLocal.get();
   }

   public static void set (UserLoginQuery query) {
      threadLocal.set(query);
   }

   public static void remove () {
      threadLocal.remove();
   }
}
