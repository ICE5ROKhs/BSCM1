package com.bscm.controller;

import com.bscm.common.Result;
import com.bscm.entity.User;
import com.bscm.logging.BusinessLogger;
import com.bscm.service.UserService;
import com.bscm.service.VerificationCodeService;
import com.bscm.util.JwtUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final UserService userService;
  private final VerificationCodeService verificationCodeService;
  private final JwtUtil jwtUtil;
  private final BusinessLogger businessLogger;

  /** 发送验证码 */
  @PostMapping("/send-code")
  public Result<String> sendVerificationCode(@RequestBody SendCodeRequest request) {
    String phone = request != null ? request.getPhone() : null;
    String maskedPhone = maskPhone(phone);

    log.debug("收到发送验证码请求，手机号: {}", maskedPhone);

    try {
      // 参数验证
      if (request == null) {
        log.warn("请求体为空");
        return Result.error("请求参数不能为空");
      }

      if (phone == null || phone.trim().isEmpty()) {
        log.warn("手机号为空");
        return Result.error("手机号不能为空");
      }

      log.debug("开始调用验证码服务，手机号: {}", maskedPhone);

      String code = verificationCodeService.sendVerificationCode(phone);

      log.debug("验证码服务调用成功，手机号: {}", maskedPhone);
      businessLogger.logBusinessEvent("发送验证码", "手机号: " + maskedPhone);

      // 开发环境返回验证码，生产环境返回null
      return Result.success("验证码已发送");
    } catch (IllegalArgumentException e) {
      log.error("发送验证码参数错误，手机号: {}, 错误: {}", maskedPhone, e.getMessage());
      businessLogger.logBusinessError(
          "发送验证码失败", "手机号: " + maskedPhone + ", 错误: " + e.getMessage(), e);
      return Result.error("发送验证码失败: " + e.getMessage());
    } catch (Exception e) {
      log.error("发送验证码异常，手机号: {}", maskedPhone, e);
      businessLogger.logBusinessError("发送验证码失败", "手机号: " + maskedPhone, e);
      return Result.error("发送验证码失败: " + e.getMessage());
    }
  }

  /** 用户注册 */
  @PostMapping("/register")
  public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
    String phone = request != null ? request.getPhone() : null;
    String maskedPhone = maskPhone(phone);

    log.debug("收到注册请求，手机号: {}", maskedPhone);

    try {
      // 参数验证
      if (request == null) {
        log.warn("注册请求体为空");
        return Result.error("请求参数不能为空");
      }

      if (phone == null || phone.trim().isEmpty()) {
        log.warn("注册时手机号为空");
        return Result.error("手机号不能为空");
      }

      if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
        log.warn("注册时密码为空，手机号: {}", maskedPhone);
        return Result.error("密码不能为空");
      }

      if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
        log.warn("注册时验证码为空，手机号: {}", maskedPhone);
        return Result.error("验证码不能为空");
      }

      // 调用服务层进行注册（业务异常会被全局异常处理器捕获）
      User user =
          userService.register(
              request.getPhone(), request.getPassword(), request.getVerificationCode());

      // 生成JWT token
      String token = jwtUtil.generateToken(user.getId(), user.getPhone());

      Map<String, Object> data = new HashMap<>();
      data.put("token", token);
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("id", user.getId());
      userMap.put("phone", user.getPhone());
      userMap.put("username", user.getUsername());
      data.put("user", userMap);

      businessLogger.logUserOperation("用户注册", user.getId(), "手机号: " + maskedPhone);
      log.info("用户注册成功，用户ID: {}, 手机号: {}", user.getId(), maskedPhone);

      return Result.success(data);
    } catch (Exception e) {
      // 这里捕获的是未预期的异常，业务异常会被全局异常处理器处理
      log.error("用户注册异常，手机号: {}", maskedPhone, e);
      businessLogger.logBusinessError("用户注册失败", "手机号: " + maskedPhone, e);
      return Result.error("注册失败，请稍后重试");
    }
  }

  /** 用户登录 */
  @PostMapping("/login")
  public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
    String phone = request != null ? request.getPhone() : null;
    String maskedPhone = maskPhone(phone);

    log.debug("收到登录请求，手机号: {}", maskedPhone);

    try {
      // 参数验证
      if (request == null) {
        log.warn("登录请求体为空");
        return Result.error("请求参数不能为空");
      }

      if (phone == null || phone.trim().isEmpty()) {
        log.warn("登录时手机号为空");
        return Result.error("手机号不能为空");
      }

      if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
        log.warn("登录时密码为空，手机号: {}", maskedPhone);
        return Result.error("密码不能为空");
      }

      // 调用服务层进行登录（业务异常会被全局异常处理器捕获）
      User user = userService.login(request.getPhone(), request.getPassword());

      // 生成JWT token
      String token = jwtUtil.generateToken(user.getId(), user.getPhone());

      Map<String, Object> data = new HashMap<>();
      data.put("token", token);
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("id", user.getId());
      userMap.put("phone", user.getPhone());
      userMap.put("username", user.getUsername());
      data.put("user", userMap);

      businessLogger.logUserOperation("用户登录", user.getId(), "手机号: " + maskedPhone);
      log.info("用户登录成功，用户ID: {}, 手机号: {}", user.getId(), maskedPhone);

      return Result.success(data);
    } catch (Exception e) {
      // 这里捕获的是未预期的异常，业务异常会被全局异常处理器处理
      log.error("用户登录异常，手机号: {}", maskedPhone, e);
      businessLogger.logBusinessWarning(
          "用户登录失败", "手机号: " + maskedPhone + ", 原因: " + e.getMessage());
      return Result.error("登录失败，请稍后重试");
    }
  }

  /** 验证码登录 */
  @PostMapping("/quick-login")
  public Result<Map<String, Object>> quickLogin(@RequestBody QuickLoginRequest request) {
    String phone = request != null ? request.getPhone() : null;
    String maskedPhone = maskPhone(phone);

    log.debug("收到验证码登录请求，手机号: {}", maskedPhone);

    try {
      // 参数验证
      if (request == null) {
        log.warn("验证码登录请求体为空");
        return Result.error("请求参数不能为空");
      }

      if (phone == null || phone.trim().isEmpty()) {
        log.warn("验证码登录时手机号为空");
        return Result.error("手机号不能为空");
      }

      if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
        log.warn("验证码登录时验证码为空，手机号: {}", maskedPhone);
        return Result.error("验证码不能为空");
      }

      // 调用服务层进行验证码登录（业务异常会被全局异常处理器捕获）
      User user = userService.loginByCode(request.getPhone(), request.getVerificationCode());

      // 生成JWT token
      String token = jwtUtil.generateToken(user.getId(), user.getPhone());

      Map<String, Object> data = new HashMap<>();
      data.put("token", token);
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("id", user.getId());
      userMap.put("phone", user.getPhone());
      userMap.put("username", user.getUsername());
      data.put("user", userMap);

      businessLogger.logUserOperation("验证码登录", user.getId(), "手机号: " + maskedPhone);
      log.info("验证码登录成功，用户ID: {}, 手机号: {}", user.getId(), maskedPhone);

      return Result.success(data);
    } catch (Exception e) {
      // 这里捕获的是未预期的异常，业务异常会被全局异常处理器处理
      log.error("验证码登录异常，手机号: {}", maskedPhone, e);
      businessLogger.logBusinessWarning(
          "验证码登录失败", "手机号: " + maskedPhone + ", 原因: " + e.getMessage());
      return Result.error("登录失败，请稍后重试");
    }
  }

  /** 重置密码 */
  @PostMapping("/reset-password")
  public Result<String> resetPassword(@RequestBody ResetPasswordRequest request) {
    String phone = request != null ? request.getPhone() : null;
    String maskedPhone = maskPhone(phone);

    log.debug("收到重置密码请求，手机号: {}", maskedPhone);

    try {
      // 参数验证
      if (request == null) {
        log.warn("重置密码请求体为空");
        return Result.error("请求参数不能为空");
      }

      if (phone == null || phone.trim().isEmpty()) {
        log.warn("重置密码时手机号为空");
        return Result.error("手机号不能为空");
      }

      if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
        log.warn("重置密码时新密码为空，手机号: {}", maskedPhone);
        return Result.error("新密码不能为空");
      }

      if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
        log.warn("重置密码时验证码为空，手机号: {}", maskedPhone);
        return Result.error("验证码不能为空");
      }

      // 调用服务层重置密码（业务异常会被全局异常处理器捕获）
      userService.resetPassword(
          request.getPhone(), request.getNewPassword(), request.getVerificationCode());

      businessLogger.logBusinessEvent("重置密码", "手机号: " + maskedPhone);
      log.info("重置密码成功，手机号: {}", maskedPhone);

      return Result.success("密码重置成功");
    } catch (Exception e) {
      // 这里捕获的是未预期的异常，业务异常会被全局异常处理器处理
      log.error("重置密码异常，手机号: {}", maskedPhone, e);
      businessLogger.logBusinessError("重置密码失败", "手机号: " + maskedPhone, e);
      return Result.error("重置密码失败，请稍后重试");
    }
  }

  /** 隐藏手机号中间部分 */
  private String maskPhone(String phone) {
    if (phone == null || phone.length() < 7) {
      return phone;
    }
    return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
  }

  @Data
  static class SendCodeRequest {
    private String phone;
  }

  @Data
  static class RegisterRequest {
    private String phone;
    private String password;
    private String verificationCode;
  }

  @Data
  static class LoginRequest {
    private String phone;
    private String password;
  }

  @Data
  static class QuickLoginRequest {
    private String phone;
    private String verificationCode;
  }

  @Data
  static class ResetPasswordRequest {
    private String phone;
    private String newPassword;
    private String verificationCode;
  }
}
