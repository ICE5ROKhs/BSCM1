package com.bscm.service.impl;

import com.bscm.service.EmbeddingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

  private static final String EMBEDDING_API_BASE_URL = "https://chatapi.zjt66.top/v1";
  private static final String EMBEDDING_API_KEY = "sk-QfccpUybEFZ3iGB9rzzukWekBgb0fkaS8Skcy4tyuM8TY5Yf";
  private static final String EMBEDDING_MODEL = "text-embedding-3-small"; // 1536维

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public EmbeddingServiceImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
  }

  @Override
  public List<Double> getEmbedding(String text) {
    try {
      if (text == null || text.trim().isEmpty()) {
        log.warn("文本为空，无法生成向量");
        return new ArrayList<>();
      }

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", EMBEDDING_MODEL);
      requestBody.put("input", text.trim());

      String requestBodyJson = objectMapper.writeValueAsString(requestBody);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(EMBEDDING_API_BASE_URL + "/embeddings"))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + EMBEDDING_API_KEY)
              .timeout(Duration.ofSeconds(30))
              .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

      if (response.statusCode() != 200) {
        String errorBody = response.body();
        log.error("Embedding API返回错误状态码: {}, 错误信息: {}", response.statusCode(), errorBody);
        throw new RuntimeException("Embedding API服务暂时不可用，请稍后重试。错误代码: " + response.statusCode());
      }

      JsonNode jsonNode = objectMapper.readTree(response.body());
      JsonNode dataNode = jsonNode.path("data");
      if (dataNode.isArray() && dataNode.size() > 0) {
        JsonNode embeddingNode = dataNode.get(0).path("embedding");
        if (embeddingNode.isArray()) {
          List<Double> embedding = new ArrayList<>();
          for (JsonNode valueNode : embeddingNode) {
            embedding.add(valueNode.asDouble());
          }
          log.debug("成功生成向量，维度: {}", embedding.size());
          return embedding;
        }
      }

      log.error("Embedding API返回格式异常: {}", response.body());
      throw new RuntimeException("Embedding API返回格式异常");
    } catch (Exception e) {
      log.error("生成向量失败: {}", e.getMessage(), e);
      throw new RuntimeException("生成向量失败: " + e.getMessage(), e);
    }
  }
}

