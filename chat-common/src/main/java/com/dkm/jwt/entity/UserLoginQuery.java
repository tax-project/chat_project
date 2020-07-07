package com.dkm.jwt.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author qf
 * @Date 2019/9/19
 * @Version 1.0
 */
@Data
public class UserLoginQuery implements Serializable {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号
     */
    private String userName;
}
