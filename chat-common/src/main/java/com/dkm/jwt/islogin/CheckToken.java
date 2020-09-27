package com.dkm.jwt.islogin;

import java.lang.annotation.*;

/**
 * @Author qf
 * @Date 2019/9/24
 * @Version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckToken {
}
