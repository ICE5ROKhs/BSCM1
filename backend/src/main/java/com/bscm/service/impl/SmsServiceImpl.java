package com.bscm.service.impl;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.bscm.config.AliyunSmsProperties;
import com.bscm.service.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import darabonba.core.client.ClientOverrideConfiguration;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

  private final AliyunSmsProperties smsProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public boolean sendVerificationCode(String phone, String code) {
    log.debug("开始发送短信验证码，手机号: {}, 验证码: {}", phone, code);

    try {
      // 配置凭证
      StaticCredentialProvider provider =
          StaticCredentialProvider.create(
              Credential.builder()
                  .accessKeyId(smsProperties.getAccessKeyId())
                  .accessKeySecret(smsProperties.getAccessKeySecret())
                  .build());

      // 创建客户端
      try (AsyncClient client =
          AsyncClient.builder()
              .region(smsProperties.getRegion())
              .credentialsProvider(provider)
              .overrideConfiguration(
                  ClientOverrideConfiguration.create().setEndpointOverride("dysmsapi.aliyuncs.com"))
              .build()) {

        // 构建请求参数
        String templateParam = String.format("{\"code\":\"%s\"}", code);
        SendSmsRequest sendSmsRequest =
            SendSmsRequest.builder()
                .signName(smsProperties.getSignName())
                .templateCode(smsProperties.getTemplateCode())
                .phoneNumbers(phone)
                .templateParam(templateParam)
                .build();

        // 发送短信（同步方式）
        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        SendSmsResponse resp = response.get();

        try {
          log.debug("阿里云短信API响应: {}", objectMapper.writeValueAsString(resp));
        } catch (Exception e) {
          log.debug("阿里云短信API响应解析失败", e);
        }

        // 检查响应状态
        if (resp != null && resp.getBody() != null) {
          String responseCode = resp.getBody().getCode();
          String message = resp.getBody().getMessage();

          if ("OK".equals(responseCode)) {
            log.info("短信发送成功，手机号: {}", phone);
            return true;
          } else {
            log.error(
                "短信发送失败，手机号: {}, 错误码: {}, 错误信息: {}, RequestId: {}",
                phone,
                responseCode,
                message,
                resp.getBody().getRequestId());
            return false;
          }
        } else {
          log.error("短信发送失败，响应为空，手机号: {}", phone);
          return false;
        }
      }
    } catch (com.aliyun.sdk.gateway.pop.exception.PopClientException e) {
      log.error(
          "阿里云短信API调用异常，手机号: {}, 错误信息: {}, RequestId: {}", phone, e.getMessage(), e.getRequestId());
      return false;
    } catch (Exception e) {
      log.error("发送短信异常，手机号: {}", phone, e);
      return false;
    }
  }
}
