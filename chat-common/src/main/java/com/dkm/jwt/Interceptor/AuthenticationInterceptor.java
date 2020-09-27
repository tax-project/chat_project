package com.dkm.jwt.Interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.jwt.islogin.CheckToken;
import com.dkm.jwt.islogin.LoginToken;
import com.dkm.utils.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;


/**
 * @Author qf
 * @Date 2019/9/24
 * @Version 1.0
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private LocalUser user;

    private String userId = "id";

    private String key = "Svpu9MogFMk4NRpN1ZMrJNaXAOckyGegdWDDimoGuFYo";

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {

        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();

        // 从 http 请求头中取出 token
        //检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(CheckToken.class)) {
            String token = getToken(httpServletRequest);
            return checkAuthorizationToken(method, token);
        }

        return true;
    }

    private String getAuthorizationToken (HttpServletRequest httpServletRequest) {
        String token1 = httpServletRequest.getHeader("Authorization");
        if (StringUtils.isNotBlank(token1)) {
            return token1.substring(7);
        }
        return null;
    }

    private String getToken (HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader("token");
    }

    private Boolean loginAuthorizationToken ( Method method) {
        //检查是否有LoginToken注释，有则跳过认证
        if (method.isAnnotationPresent(LoginToken.class)) {
            LoginToken loginToken = method.getAnnotation(LoginToken.class);
            if (loginToken.required()) {
                return true;
            }
        }
        return false;
    }

    private Boolean checkAuthorizationToken (Method method, String token) {
        CheckToken checkToken = method.getAnnotation(CheckToken.class);
        if (checkToken.required()) {
            // 执行认证
            if (StringUtils.isBlank(token)) {
                throw new ApplicationException(CodeType.OVENDU_ERROR,"token 为空");
            }

            UserLoginQuery query = getUserInfo(token);

            Boolean verify = isVerify(token, query);
            if (!verify) {
                throw new ApplicationException(CodeType.OVENDU_ERROR, "身份验证失败");
            }

            //去掉过期时间
//            return expDate (token);
        }
        return true;
    }

    private UserLoginQuery getUserInfo (String token) {
        // 获取 token 中的 user信息
        UserLoginQuery query = new UserLoginQuery();
        try {
            query.setId(JWT.decode(token).getClaim("id").asLong());
            query.setUserName(JWT.decode(token).getClaim("userName").asString());

        } catch (JWTDecodeException j) {
            throw new ApplicationException(CodeType.OVENDU_ERROR,"token错误");
        }

        user.setUser(query);
        return query;
    }

    private Boolean expDate (String token) {
        //获得解密后claims对象
        Date date = new Date();
        String key = getKey();
        Claims jwt = getClaims(token,key);

        String audience = jwt.getAudience();
        Long erp = Long.parseLong(audience);
        Date erpDate = new Date(erp);
        //判断token时间是否过期
        if (erpDate.before(date)) {
            throw new ApplicationException(CodeType.OVENDU_ERROR);
        }

        return true;
    }

    private Boolean isVerify(String token, UserLoginQuery user) {
        //签名秘钥，和生成的签名的秘钥一模一样
        String key = getKey();

        //得到DefaultJwtParser
        Claims claims = getClaims (token, key);

        if (claims.get(userId).equals(user.getId())) {
            return true;
        }

        return false;
    }

    private Claims getClaims (String token, String key) {
        //得到Claims
        Claims claims;
        try {
            claims = Jwts.parser()
                  //设置签名的秘钥
                  .setSigningKey(key)
                  //设置需要解析的jwt
                  .parseClaimsJws(token).getBody();
        }catch (Exception e) {
            throw new ApplicationException(CodeType.OVENDU_ERROR,"token错误");
        }

        return claims;
    }

    /**
     *  得到密钥
     * @return 返回密钥
     */
    private String getKey () {
        return key;
    }
}
