package com.bscm.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {

  /** AccessKey ID */
  private String accessKeyId;

  /** AccessKey Secret */
  private String accessKeySecret;

  /** 短信签名 */
  private String signName;

  /** 短信模板代码 */
  private String templateCode;

  /** 区域 */
  private String region = "cn-qingdao";

  @PostConstruct
  public void init() {
    // 优先从环境变量获取，如果没有环境变量则使用配置文件中的值
    String envAccessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
    if (envAccessKeyId != null && !envAccessKeyId.trim().isEmpty()) {
      this.accessKeyId = envAccessKeyId;
      log.info("从环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 读取 AccessKeyId");
    } else {
      // 如果环境变量不存在，使用配置文件中的值（可能为空）
      if (this.accessKeyId == null || this.accessKeyId.trim().isEmpty()) {
        log.warn("警告: ALIBABA_CLOUD_ACCESS_KEY_ID 环境变量未设置，且配置文件中也没有值，短信功能将无法使用");
      } else {
        log.info("使用配置文件中的 AccessKeyId（建议使用环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID）");
      }
    }

    String envAccessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
    if (envAccessKeySecret != null && !envAccessKeySecret.trim().isEmpty()) {
      this.accessKeySecret = envAccessKeySecret;
      log.info("从环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET 读取 AccessKeySecret");
    } else {
      // 如果环境变量不存在，使用配置文件中的值（可能为空）
      if (this.accessKeySecret == null || this.accessKeySecret.trim().isEmpty()) {
        log.warn("警告: ALIBABA_CLOUD_ACCESS_KEY_SECRET 环境变量未设置，且配置文件中也没有值，短信功能将无法使用");
      } else {
        log.info("使用配置文件中的 AccessKeySecret（建议使用环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET）");
      }
    }
  }
}
