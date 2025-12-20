package com.bscm.service.impl;

import com.bscm.entity.VerificationCode;
import com.bscm.repository.VerificationCodeRepository;
import com.bscm.service.VerificationCodeService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

  private final VerificationCodeRepository verificationCodeRepository;
  private final Random random = new Random();

  @Override
  @Transactional
  public String sendVerificationCode(String phone) {
    log.debug("开始发送验证码，手机号: {}", phone);

    try {
      // 参数验证
      if (phone == null || phone.trim().isEmpty()) {
        log.warn("手机号为空，无法发送验证码");
        throw new IllegalArgumentException("手机号不能为空");
      }

      if (!phone.matches("^1[3-9]\\d{9}$")) {
        log.warn("手机号格式不正确: {}", phone);
        throw new IllegalArgumentException("手机号格式不正确");
      }

      log.debug("手机号验证通过: {}", phone);

      // 生成6位随机验证码
      String code = String.format("%06d", random.nextInt(1000000));
      log.debug("生成验证码成功: {}", code);

      // 创建验证码记录
      VerificationCode verificationCode = new VerificationCode();
      verificationCode.setPhone(phone);
      verificationCode.setCode(code);
      verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
      verificationCode.setUsed(false);

      log.debug("准备保存验证码记录到数据库，手机号: {}, 过期时间: {}", phone, verificationCode.getExpiresAt());

      try {
        verificationCodeRepository.save(verificationCode);
        log.debug("验证码记录保存成功，ID: {}", verificationCode.getId());
      } catch (Exception e) {
        log.error("保存验证码记录到数据库失败，手机号: {}", phone, e);
        throw new RuntimeException("保存验证码失败: " + e.getMessage(), e);
      }

      // 这里应该调用短信服务发送验证码
      // 为了演示，我们只在日志中输出
      log.info("发送验证码到手机号 {}: {}", phone, code);
      log.debug("验证码发送流程完成，手机号: {}", phone);

      // 开发环境返回验证码，生产环境应该返回null
      return code;
    } catch (IllegalArgumentException e) {
      log.error("发送验证码参数错误，手机号: {}, 错误: {}", phone, e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("发送验证码异常，手机号: {}", phone, e);
      throw new RuntimeException("发送验证码失败: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public boolean verifyCode(String phone, String code) {
    Optional<VerificationCode> codeOpt =
        verificationCodeRepository.findValidCode(phone, code, LocalDateTime.now());

    if (codeOpt.isPresent()) {
      // 标记为已使用
      verificationCodeRepository.markAsUsed(phone, code);
      return true;
    }

    return false;
  }

  /** 定时清理过期验证码（每小时执行一次） */
  @Scheduled(fixedRate = 3600000)
  @Transactional
  public void cleanExpiredCodes() {
    verificationCodeRepository.deleteExpiredCodes(LocalDateTime.now());
  }
}
