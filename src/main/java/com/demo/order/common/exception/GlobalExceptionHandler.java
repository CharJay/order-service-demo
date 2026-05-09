package com.demo.order.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}，错误码：{}，数据：{}", e.getMessage(), e.getCode(), e.getData());
        
        ErrorResponse response = ErrorResponse.builder()
                .code(e.getCode())
                .message(e.getMessage())
                .data(e.getData())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数验证异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数验证异常：{}", e.getMessage());
        
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        ErrorResponse response = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("参数验证失败")
                .data(errors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.warn("绑定异常：{}", e.getMessage());
        
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        ErrorResponse response = ErrorResponse.builder()
                .code("BIND_ERROR")
                .message("参数绑定失败")
                .data(errors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束违反异常：{}", e.getMessage());
        
        List<String> errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        ErrorResponse response = ErrorResponse.builder()
                .code("CONSTRAINT_VIOLATION")
                .message("约束违反")
                .data(errors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配异常：{}", e.getMessage());
        
        String error = String.format("参数 '%s' 类型不匹配，期望类型：%s", 
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        
        ErrorResponse response = ErrorResponse.builder()
                .code("ARGUMENT_TYPE_MISMATCH")
                .message(error)
                .data(Map.of("parameter", e.getName(), "expectedType", e.getRequiredType()))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常：{}", e.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
                .code("ILLEGAL_ARGUMENT")
                .message(e.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.warn("非法状态异常：{}", e.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
                .code("ILLEGAL_STATE")
                .message(e.getMessage())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("系统异常：", e);
        
        ErrorResponse response = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("服务器内部错误")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 错误响应类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
        private Object data;
        private Long timestamp;
        
        {
            // 初始化块，确保timestamp有值
            if (timestamp == null) {
                timestamp = System.currentTimeMillis();
            }
        }
    }
}