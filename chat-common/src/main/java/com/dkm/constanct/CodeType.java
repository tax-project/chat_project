package com.dkm.constanct;

/**
 * @Author qf
 * @Date 2019/8/20
 * @Version 1.0
 */
public enum CodeType {

    /**
     * 成功
     */
    SUCCESS(0, "操作成功", 200),

    /**
     * 用户名重复
     */
    SUCC_ERROR(1,"名字重复",200),

    /**
     * 未知错误
     */
    UNKNOWN_ERROR(999, "未知错误", 500),
    /**
     * 身份认证错误
     */
    AUTHENTICATION_ERROR(1000, "身份认证错误", 403),
    /**
     * 账号或密码错误
     */
    LOGIN_ERROR(1001, "账号或密码错误", 500),
    /**
     * 参数错误
     */
    PARAMETER_ERROR(1002, "参数错误", 500),
    /**
     * 资源未找到
     */
    RESOURCES_NOT_FIND(1003, "资源未找到", 500),
    /**
     * 资源已存在
     */
    RESOURCES_EXISTING(1004, "资源已存在", 500),
    /**
     * 验证码错误
     */
    SMS_CODE_ERROR(1005, "验证码错误", 500),
    /**
     * 业务异常
     */
    SERVICE_ERROR(1006, "业务异常", 500),
    /**
     * feign 调用异常
     */
    FEIGN_CONNECT_ERROR(1007, "网络忙，请稍后再试", 500),

    /**
     * token过期
     */
    OVENDU_ERROR(401,"token过期",401),

    /**
     * token为空
     */
    TOKENNULL_ERROR(1011,"token为空",406),

    /**
     * 解码失败
     */
    DECODING_ERROR(1012,"解码失败",405),

    /**
     * 数据库操作失败
     */
    DATABASE_ERROR(1009, "网络忙，请稍后再试", 500);

    private int code;
    private String msg;
    private int httpStatus;

    CodeType(int code, String msg, int httpStatus) {
        this.code = code;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

    public int getCode(){
        return code;
    }

    public void setCode(int code){
        this.code = code;
    }

    public String getMsg(){
        return msg;
    }

    public void setMsg(String msg){
        this.msg = msg;
    }

    public int getHttpStatus(){
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus){
        this.httpStatus = httpStatus;
    }
}
