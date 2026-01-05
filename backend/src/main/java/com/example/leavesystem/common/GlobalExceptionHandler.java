package com.example.leavesystem.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把常见异常映射为合适的 HTTP 状态码 + Result
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) 参数校验 / 状态机非法 等主动抛出的错误
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        Result<Void> body = Result.failure(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 2) 权限不足（你如果有自定义 AccessDeniedException，建议加这一条）
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
        Result<Void> body = Result.error(403, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 3) 未登录 / token 无效（如果你拦截器里会抛 UnauthorizedException，建议加）
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(UnauthorizedException ex) {
        Result<Void> body = Result.error(401, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 4) 兜底的异常处理（生产环境建议不要把异常类名返回给前端）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        ex.printStackTrace();
        String msg = ex.getClass().getName() + ": " + ex.getMessage();
        Result<Void> body = Result.failure(msg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(404, "接口不存在"));
    }

}
