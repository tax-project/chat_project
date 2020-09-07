package com.dkm.jwt.contain;



import com.dkm.jwt.entity.UserLoginQuery;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author qf
 * @Date 2019/9/25
 * @Version 1.0
 */
@Component
public class LocalUser {

    /**
     * 从token获得用户信息
     * @return
     */
    public void setUser (UserLoginQuery vo) {
        ThreadLocalMap.set(vo);
    }


    /**
     * 得到用户信息
     * @return
     */
    public UserLoginQuery getUser () {
        UserLoginQuery query = ThreadLocalMap.get();
        ThreadLocalMap.remove();
        return query;
    }
}
