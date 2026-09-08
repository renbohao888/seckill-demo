package com.renbohao.seckill.common;

import lombok.Data;

/**
 * 统一接口返回结构。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 业务码：200-成功，400-业务失败（如库存不足、已被抢光、重复请求等） */
    private int code;
    /** 提示信息 */
    private String msg;
    /** 业务数据 */
    private T data;

    public static <T> Result<T> ok(T data, String msg) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.setCode(400);
        r.setMsg(msg);
        return r;
    }
}
