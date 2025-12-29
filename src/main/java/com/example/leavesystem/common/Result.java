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

    // 返回指定错误码
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 常用：成功但无 data */
    public static Result<Void> ok() {              // ✅ 修改：不再返回 null
        return Result.success(null);
    }

    /** 常用：失败（等同 failure） */
    public static Result<Void> fail(String message) { // ✅ 修改：参数名改成 message
        return Result.failure(message);
    }
}
