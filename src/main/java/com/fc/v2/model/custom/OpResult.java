package com.fc.v2.model.custom;

/**
 * 核签四开操作结果：被拒不只回 null，还得把卡在哪一开、为什么卡住讲明。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class OpResult<T> {

    private boolean ok;
    private String msg;
    private T data;

    public static <T> OpResult<T> ok(T data) {
        OpResult<T> r = new OpResult<T>();
        r.ok = true;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> OpResult<T> ok(String msg, T data) {
        OpResult<T> r = new OpResult<T>();
        r.ok = true;
        r.msg = msg;
        r.data = data;
        return r;
    }

    public static <T> OpResult<T> fail(String msg) {
        OpResult<T> r = new OpResult<T>();
        r.ok = false;
        r.msg = msg;
        return r;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
