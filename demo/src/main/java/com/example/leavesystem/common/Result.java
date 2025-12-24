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
}
