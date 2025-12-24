package com.example.leavesystem.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 参数校验等主动抛出的错误
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        Result<Void> body = Result.failure(ex.getMessage());
        // 返回 400，更符合语义
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 兜底的异常处理（不要泄露堆栈给前端）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        ex.printStackTrace();  // 控制台打印完整堆栈
        // 把异常类名 + message 暂时返回给前端，便于你看到具体错哪
        String msg = ex.getClass().getName() + ": " + ex.getMessage();
        Result<Void> body = Result.failure(msg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
