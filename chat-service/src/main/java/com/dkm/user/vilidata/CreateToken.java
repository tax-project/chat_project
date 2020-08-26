package com.dkm.user.vilidata;

import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.user.entity.User;
import com.dkm.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author qf
 * @date 2020/4/13
 * @vesion 1.0
 **/
@Component
public class CreateToken {

   @Autowired
   private JwtUtil jwtUtil;

   public String getToken (User user) {
      UserLoginQuery query = new UserLoginQuery();
      query.setId(user.getId());
      query.setUserName(user.getUserName());

      //24小时过期时间 * 30天
      return jwtUtil.createJwt(query);
   }



}
