package com.bscm.service.impl;

import com.bscm.common.ChatResponse;
import com.bscm.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

  private static final String CHAT_API_BASE_URL = "https://chatapi.zjt66.top/v1";
  private static final String CHAT_API_KEY = "sk-QfccpUybEFZ3iGB9rzzukWekBgb0fkaS8Skcy4tyuM8TY5Yf";
  // 注意：真实环境中不建议把密钥写死在代码里，这里按题目要求直接使用
  private static final String CHAT_MODEL = "gpt-4o-mini";

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public ChatServiceImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
  }

  @Override
  public ChatResponse sendMessage(List<Map<String, String>> messages) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", CHAT_MODEL);
      requestBody.put("messages", messages);
      requestBody.put("stream", false);
      requestBody.put("temperature", 0.7);
      // 移除group参数，因为API返回"无可用渠道"错误

      String requestBodyJson = objectMapper.writeValueAsString(requestBody);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(CHAT_API_BASE_URL + "/chat/completions"))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + CHAT_API_KEY)
              .timeout(Duration.ofSeconds(60))
              .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

      if (response.statusCode() != 200) {
        String errorBody = response.body();
        log.error("AI API返回错误状态码: {}, 错误信息: {}", response.statusCode(), errorBody);
        throw new RuntimeException("AI API服务暂时不可用，请稍后重试。错误代码: " + response.statusCode());
      }

      JsonNode jsonNode = objectMapper.readTree(response.body());
      String content = jsonNode.path("choices").path(0).path("message").path("content").asText("");

      log.info("AI回复成功，内容长度: {}", content.length());

      // 提取话题主题
      String topic = extractTopic(content);
      // 如果提取到话题主题，从内容中移除JSON格式的话题主题
      if (topic != null) {
        content = content.replaceAll("\\{\\s*\"topic\"\\s*:\\s*\"[^\"]+\"\\s*\\}\\s*$", "").trim();
      }

      return new ChatResponse(content, topic);
    } catch (Exception e) {
      log.error("发送聊天消息失败", e);
      throw new RuntimeException("发送消息失败: " + e.getMessage(), e);
    }
  }

  /** 从AI回复内容中提取话题主题 支持格式：{"topic": "话题主题"} */
  private String extractTopic(String content) {
    if (content == null || content.isEmpty()) {
      return null;
    }

    try {
      // 尝试从内容末尾提取JSON格式的话题主题
      String trimmed = content.trim();
      // 匹配 {"topic": "..."} 格式
      java.util.regex.Pattern pattern =
          java.util.regex.Pattern.compile(
              "\\{\\s*\"topic\"\\s*:\\s*\"([^\"]+)\"\\s*\\}\\s*$",
              java.util.regex.Pattern.CASE_INSENSITIVE);
      java.util.regex.Matcher matcher = pattern.matcher(trimmed);
      if (matcher.find()) {
        String topic = matcher.group(1);
        if (topic != null && !topic.isEmpty() && topic.length() <= 25) {
          log.info("提取到话题主题: {}", topic);
          return topic;
        }
      }

      // 如果没有找到JSON格式，尝试从第一条用户消息中提取关键词作为话题
      // 这里可以根据需要实现更复杂的提取逻辑
    } catch (Exception e) {
      log.debug("提取话题主题失败", e);
    }

    return null;
  }

  @Override
  public void sendMessageStream(List<Map<String, String>> messages, ChunkHandler onChunk) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", CHAT_MODEL);
      requestBody.put("messages", messages);
      requestBody.put("stream", true);
      requestBody.put("temperature", 0.7);
      // 移除group参数，因为API返回"无可用渠道"错误

      String requestBodyJson = objectMapper.writeValueAsString(requestBody);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(CHAT_API_BASE_URL + "/chat/completions"))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + CHAT_API_KEY)
              .timeout(Duration.ofSeconds(120))
              .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
              .build();

      HttpResponse<InputStream> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() != 200) {
        // 读取错误响应体
        String errorBody = "";
        String errorMessage = "AI API服务暂时不可用，请稍后重试";
        try (InputStream errorStream = response.body()) {
          errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
          // 尝试解析错误JSON
          try {
            JsonNode errorJson = objectMapper.readTree(errorBody);
            JsonNode errorNode = errorJson.path("error");
            if (errorNode.has("message")) {
              errorMessage = errorNode.path("message").asText(errorMessage);
            } else if (errorNode.has("code")) {
              String errorCode = errorNode.path("code").asText();
              errorMessage = "AI API错误: " + errorCode;
            }
          } catch (Exception e) {
            log.debug("解析错误响应JSON失败", e);
          }
        } catch (Exception e) {
          log.debug("读取错误响应体失败", e);
        }
        log.error("AI API返回错误状态码: {}, 错误信息: {}", response.statusCode(), errorBody);
        throw new RuntimeException(errorMessage + " (错误代码: " + response.statusCode() + ")");
      }

      // 处理流式响应
      try (InputStream inputStream = response.body()) {
        StringBuilder buffer = new StringBuilder();
        byte[] bytes = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(bytes)) != -1) {
          buffer.append(new String(bytes, 0, bytesRead, StandardCharsets.UTF_8));

          // 处理完整的行
          String data = buffer.toString();
          String[] lines = data.split("\n", -1);
          buffer.setLength(0);
          buffer.append(lines[lines.length - 1]); // 保留最后不完整的行

          for (int i = 0; i < lines.length - 1; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
              continue;
            }

            if (line.startsWith("data: ")) {
              String jsonData = line.substring(6);
              if ("[DONE]".equals(jsonData)) {
                return;
              }

              try {
                JsonNode jsonNode = objectMapper.readTree(jsonData);
                String content =
                    jsonNode.path("choices").path(0).path("delta").path("content").asText("");
                if (!content.isEmpty() && onChunk != null) {
                  try {
                    onChunk.handle(content);
                  } catch (RuntimeException e) {
                    // 如果chunk handler抛出异常（如SSE连接关闭），重新抛出以中断流处理
                    log.warn("chunk handler抛出异常，中断流处理", e);
                    throw e;
                  }
                }
              } catch (JsonProcessingException e) {
                log.debug("解析JSON数据失败: {}", jsonData, e);
              }
            }
          }
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("发送流式聊天消息失败", e);
      throw new RuntimeException("发送消息失败: " + e.getMessage(), e);
    }
  }
}
