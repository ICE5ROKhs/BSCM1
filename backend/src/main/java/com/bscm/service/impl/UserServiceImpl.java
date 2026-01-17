package com.bscm.service.impl;

import com.bscm.entity.User;
import com.bscm.exception.BusinessException;
import com.bscm.repository.UserRepository;
import com.bscm.service.UserService;
import com.bscm.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final VerificationCodeService verificationCodeService;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public User register(String phone, String password, String verificationCode) {
    log.debug("开始用户注册流程，手机号: {}", maskPhone(phone));

    // 参数验证
    if (phone == null || phone.trim().isEmpty()) {
      throw new BusinessException("手机号不能为空");
    }

    if (password == null || password.trim().isEmpty()) {
      throw new BusinessException("密码不能为空");
    }

    if (password.length() < 6) {
      throw new BusinessException("密码长度不能少于6位");
    }

    if (verificationCode == null || verificationCode.trim().isEmpty()) {
      throw new BusinessException("验证码不能为空");
    }

    // 验证验证码
    if (!verificationCodeService.verifyCode(phone, verificationCode)) {
      log.warn("验证码验证失败，手机号: {}", maskPhone(phone));
      throw new BusinessException("验证码错误或已过期");
    }

    // 检查手机号是否已注册
    if (userRepository.existsByPhone(phone)) {
      log.warn("手机号已被注册，手机号: {}", maskPhone(phone));
      throw new BusinessException("该手机号已被注册");
    }

    try {
      // 创建新用户
      User user = new User();
      user.setPhone(phone);
      user.setPassword(passwordEncoder.encode(password));
      // 设置默认用户名（使用手机号作为默认用户名）
      user.setUsername(phone);
      user.setRole("USER"); // 设置默认角色

      User savedUser = userRepository.save(user);
      log.info("用户注册成功，用户ID: {}, 手机号: {}", savedUser.getId(), maskPhone(phone));
      return savedUser;
    } catch (Exception e) {
      log.error("用户注册失败，手机号: {}", maskPhone(phone), e);
      throw new BusinessException("注册失败: " + e.getMessage(), e);
    }
  }

  @Override
  public User login(String phone, String password) {
    log.debug("开始用户登录流程，手机号: {}", maskPhone(phone));

    // 参数验证
    if (phone == null || phone.trim().isEmpty()) {
      throw new BusinessException("手机号不能为空");
    }

    if (password == null || password.trim().isEmpty()) {
      throw new BusinessException("密码不能为空");
    }

    // 查找用户
    User user =
        userRepository
            .findByPhone(phone)
            .orElseThrow(
                () -> {
                  log.warn("登录失败：用户不存在，手机号: {}", maskPhone(phone));
                  return new BusinessException("手机号或密码错误");
                });

    // 验证密码
    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.warn("登录失败：密码错误，手机号: {}", maskPhone(phone));
      throw new BusinessException("手机号或密码错误");
    }

    log.info("用户登录成功，用户ID: {}, 手机号: {}", user.getId(), maskPhone(phone));
    return user;
  }

  @Override
  public User loginByCode(String phone, String verificationCode) {
    log.debug("开始验证码登录流程，手机号: {}", maskPhone(phone));

    // 参数验证
    if (phone == null || phone.trim().isEmpty()) {
      throw new BusinessException("手机号不能为空");
    }

    if (verificationCode == null || verificationCode.trim().isEmpty()) {
      throw new BusinessException("验证码不能为空");
    }

    // 验证验证码
    if (!verificationCodeService.verifyCode(phone, verificationCode)) {
      log.warn("验证码登录失败：验证码错误或已过期，手机号: {}", maskPhone(phone));
      throw new BusinessException("验证码错误或已过期");
    }

    // 查找用户，如果不存在则创建
    User user = userRepository.findByPhone(phone).orElse(null);
    if (user == null) {
      log.info("验证码登录时用户不存在，自动创建用户，手机号: {}", maskPhone(phone));
      user = new User();
      user.setPhone(phone);
      user.setUsername(phone);
      user.setPassword(passwordEncoder.encode("")); // 设置空密码，验证码登录不需要密码
      user.setRole("USER"); // 设置默认角色
      user = userRepository.save(user);
    }

    log.info("验证码登录成功，用户ID: {}, 手机号: {}", user.getId(), maskPhone(phone));
    return user;
  }

  @Override
  @Transactional
  public void resetPassword(String phone, String newPassword, String verificationCode) {
    log.debug("开始重置密码流程，手机号: {}", maskPhone(phone));

    // 参数验证
    if (phone == null || phone.trim().isEmpty()) {
      throw new BusinessException("手机号不能为空");
    }

    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new BusinessException("新密码不能为空");
    }

    if (newPassword.length() < 6) {
      throw new BusinessException("密码长度不能少于6位");
    }

    if (verificationCode == null || verificationCode.trim().isEmpty()) {
      throw new BusinessException("验证码不能为空");
    }

    // 验证验证码
    if (!verificationCodeService.verifyCode(phone, verificationCode)) {
      log.warn("重置密码失败：验证码错误或已过期，手机号: {}", maskPhone(phone));
      throw new BusinessException("验证码错误或已过期");
    }

    // 查找用户
    User user =
        userRepository
            .findByPhone(phone)
            .orElseThrow(
                () -> {
                  log.warn("重置密码失败：用户不存在，手机号: {}", maskPhone(phone));
                  return new BusinessException("用户不存在");
                });

    // 更新密码
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    log.info("密码重置成功，用户ID: {}, 手机号: {}", user.getId(), maskPhone(phone));
  }

  @Override
  public User findByPhone(String phone) {
    if (phone == null || phone.trim().isEmpty()) {
      throw new BusinessException("手机号不能为空");
    }
    return userRepository.findByPhone(phone).orElseThrow(() -> new BusinessException("用户不存在"));
  }

  @Override
  @Transactional
  public User createUser(String phone, String password) {
    log.debug("开始创建用户，手机号: {}", maskPhone(phone));

    // 参数验证
    if (phone == null || phone.trim().isEmpty()) {
      throw new BusinessException("手机号不能为空");
    }

    if (password == null || password.trim().isEmpty()) {
      throw new BusinessException("密码不能为空");
    }

    // 检查手机号是否已注册
    if (userRepository.existsByPhone(phone)) {
      log.warn("手机号已被注册，手机号: {}", maskPhone(phone));
      throw new BusinessException("该手机号已被注册");
    }

    try {
      // 创建新用户
      User user = new User();
      user.setPhone(phone);
      user.setPassword(passwordEncoder.encode(password));
      // 设置默认用户名（使用手机号作为默认用户名）
      user.setUsername(phone);
      user.setRole("USER"); // 设置默认角色

      User savedUser = userRepository.save(user);
      log.info("用户创建成功，用户ID: {}, 手机号: {}", savedUser.getId(), maskPhone(phone));
      return savedUser;
    } catch (Exception e) {
      log.error("用户创建失败，手机号: {}", maskPhone(phone), e);
      throw new BusinessException("创建用户失败: " + e.getMessage(), e);
    }
  }

  /** 隐藏手机号中间部分 */
  private String maskPhone(String phone) {
    if (phone == null || phone.length() < 7) {
      return phone;
    }
    return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
  }
}
