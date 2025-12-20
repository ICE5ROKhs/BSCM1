package com.bscm.exception;

import com.bscm.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理器 统一处理应用中的异常，返回友好的错误信息 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /** 处理业务异常 */
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<?> handleBusinessException(BusinessException e) {
    log.warn("业务异常: {}", e.getMessage());
    return Result.error(e.getMessage());
  }

  /** 处理数据完整性违反异常（如唯一约束、非空约束等） */
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    log.error("数据完整性违反异常", e);
    String message = e.getMessage();

    // 解析常见的数据库约束错误
    if (message != null) {
      if (message.contains("phone") && message.contains("unique")) {
        return Result.error("该手机号已被注册");
      }
      if (message.contains("username") && message.contains("unique")) {
        return Result.error("该用户名已被使用");
      }
      if (message.contains("username") && message.contains("not-null")) {
        return Result.error("用户名不能为空");
      }
    }

    return Result.error("数据验证失败，请检查输入信息");
  }

  /** 处理参数验证异常 */
  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<?> handleValidationException(Exception e) {
    log.warn("参数验证异常: {}", e.getMessage());

    FieldError fieldError = null;
    if (e instanceof MethodArgumentNotValidException) {
      MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
      fieldError = ex.getBindingResult().getFieldError();
    } else if (e instanceof BindException) {
      BindException ex = (BindException) e;
      fieldError = ex.getBindingResult().getFieldError();
    }

    if (fieldError != null) {
      String message = fieldError.getDefaultMessage();
      return Result.error(message != null ? message : "参数验证失败");
    }

    return Result.error("参数验证失败");
  }

  /** 处理其他未预期的异常 */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result<?> handleException(Exception e) {
    log.error("未预期的异常", e);
    return Result.error("服务器内部错误，请稍后重试");
  }
}
