package com.example.leavesystem.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "ok", data);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(-1, message, null);
    }
    // 添加error方法，用于返回指定错误码和错误信息的结果
    public static <T> Result<T> error(int code, String message) {return new Result<>(code, message, null);}

    public static Result ok() {
        return null;
    }

    public static Result fail(String 删除失败) {
        return null;
    }
}
