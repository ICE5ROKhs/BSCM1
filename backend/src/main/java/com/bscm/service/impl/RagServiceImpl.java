package com.bscm.service.impl;

import com.bscm.entity.KnowledgeBase;
import com.bscm.repository.KnowledgeBaseRepository;
import com.bscm.service.EmbeddingService;
import com.bscm.service.RagService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagServiceImpl implements RagService {

  private static final int TOP_K = 5; // 检索Top-K条相似知识
  private static final double SIMILARITY_THRESHOLD = 0.5; // 相似度阈值

  private final EmbeddingService embeddingService;
  private final KnowledgeBaseRepository knowledgeBaseRepository;
  private final ObjectMapper objectMapper;

  @Override
  public String buildEnhancedPrompt(
      String userQuestion, List<Map<String, String>> conversationHistory) {
    try {
      // 1. 将用户问题转换为向量
      List<Double> questionVector = embeddingService.getEmbedding(userQuestion);
      if (questionVector == null || questionVector.isEmpty()) {
        log.warn("向量生成失败，返回基础提示词");
        return buildBasicPrompt(userQuestion, conversationHistory);
      }

      // 2. 检索相似知识
      List<KnowledgeBase> similarKnowledge = retrieveSimilarKnowledge(questionVector);
      log.info("检索到 {} 条相似知识", similarKnowledge.size());

      // 3. 构建增强提示词
      return buildFullEnhancedPrompt(userQuestion, similarKnowledge, conversationHistory);
    } catch (Exception e) {
      log.error("构建RAG增强提示词失败", e);
      return buildBasicPrompt(userQuestion, conversationHistory);
    }
  }

  /** 检索相似知识 */
  private List<KnowledgeBase> retrieveSimilarKnowledge(List<Double> questionVector) {
    try {
      List<KnowledgeBase> allKnowledge = knowledgeBaseRepository.findAll();
      List<KnowledgeWithSimilarity> knowledgeWithSimilarity = new ArrayList<>();

      for (KnowledgeBase knowledge : allKnowledge) {
        if (knowledge.getVector() == null || knowledge.getVector().trim().isEmpty()) {
          continue;
        }

        try {
          List<Double> knowledgeVector = parseVectorFromJson(knowledge.getVector());
          if (knowledgeVector == null || knowledgeVector.isEmpty()) {
            continue;
          }

          double similarity = cosineSimilarity(questionVector, knowledgeVector);

          if (similarity >= SIMILARITY_THRESHOLD) {
            knowledgeWithSimilarity.add(new KnowledgeWithSimilarity(knowledge, similarity));
          }
        } catch (Exception e) {
          log.debug("计算知识 ID {} 的相似度失败: {}", knowledge.getId(), e.getMessage());
        }
      }

      List<KnowledgeBase> result =
          knowledgeWithSimilarity.stream()
              .sorted(
                  Comparator.comparingDouble((KnowledgeWithSimilarity k) -> k.similarity)
                      .reversed())
              .limit(TOP_K)
              .map(k -> k.knowledge)
              .collect(Collectors.toList());

      if (!result.isEmpty()) {
        String similarities =
            knowledgeWithSimilarity.stream()
                .sorted(
                    Comparator.comparingDouble((KnowledgeWithSimilarity k) -> k.similarity)
                        .reversed())
                .limit(TOP_K)
                .map(k -> String.format("%.3f", k.similarity))
                .collect(Collectors.joining(", "));
        log.debug("Top-{} 知识相似度: {}", TOP_K, similarities);
      }

      return result;
    } catch (Exception e) {
      log.error("检索相似知识失败", e);
      return new ArrayList<>();
    }
  }

  /** 构建完整的增强提示词 */
  private String buildFullEnhancedPrompt(
      String userQuestion,
      List<KnowledgeBase> similarKnowledge,
      List<Map<String, String>> conversationHistory) {

    StringBuilder prompt = new StringBuilder();

    // 系统提示词
    prompt.append("你是一位专业的AI医疗诊断助手，专注于脑干海绵状血管畸形（Brainstem Cavernous Malformation, BSCM）的诊断咨询。\n\n");

    // 检索到的相关知识
    if (!similarKnowledge.isEmpty()) {
      prompt.append("=== 相关知识库内容 ===\n");
      for (int i = 0; i < similarKnowledge.size(); i++) {
        KnowledgeBase kb = similarKnowledge.get(i);
        prompt.append(String.format("[知识%d]\n", i + 1));
        prompt.append(String.format("问题: %s\n", kb.getQuestion()));
        prompt.append(String.format("答案: %s\n\n", kb.getAnswer()));
      }
      prompt.append("请基于以上相关知识库内容回答用户问题。\n\n");
    } else {
      prompt.append("未检索到相关知识库内容，将基于医学知识回答。\n\n");
    }

    // 对话历史
    if (conversationHistory != null && !conversationHistory.isEmpty()) {
      prompt.append("=== 对话历史（最近3组） ===\n");
      int historyCount = Math.min(conversationHistory.size(), 6); // 最多3组对话（6条消息）
      int startIndex = conversationHistory.size() - historyCount;
      for (int i = startIndex; i < conversationHistory.size(); i++) {
        Map<String, String> msg = conversationHistory.get(i);
        String role = msg.get("role");
        String content = msg.get("content");
        if ("user".equals(role)) {
          prompt.append(String.format("用户: %s\n", content));
        } else if ("assistant".equals(role)) {
          prompt.append(String.format("AI: %s\n", content));
        }
      }
      prompt.append("\n");
    }

    // 用户当前问题
    prompt.append("=== 用户当前问题 ===\n");
    prompt.append(userQuestion);
    prompt.append("\n\n");

    // 回答要求
    prompt.append("请用专业但易懂的语言回答问题。\n");

    return prompt.toString();
  }

  /** 构建基础提示词（无RAG） */
  private String buildBasicPrompt(
      String userQuestion, List<Map<String, String>> conversationHistory) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("你是一位专业的AI医疗诊断助手，专注于脑干海绵状血管畸形（Brainstem Cavernous Malformation, BSCM）的诊断咨询。\n\n");
    prompt.append("未检索到相关知识库内容，将基于医学知识回答。\n\n");

    if (conversationHistory != null && !conversationHistory.isEmpty()) {
      prompt.append("=== 对话历史（最近3组） ===\n");
      int historyCount = Math.min(conversationHistory.size(), 6);
      int startIndex = conversationHistory.size() - historyCount;
      for (int i = startIndex; i < conversationHistory.size(); i++) {
        Map<String, String> msg = conversationHistory.get(i);
        String role = msg.get("role");
        String content = msg.get("content");
        if ("user".equals(role)) {
          prompt.append(String.format("用户: %s\n", content));
        } else if ("assistant".equals(role)) {
          prompt.append(String.format("AI: %s\n", content));
        }
      }
      prompt.append("\n");
    }

    prompt.append("=== 用户当前问题 ===\n");
    prompt.append(userQuestion);
    prompt.append("\n\n");
    prompt.append("请用专业但易懂的语言回答问题。\n");

    return prompt.toString();
  }

  /** 计算余弦相似度 */
  private double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
    if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
      return 0.0;
    }

    double dotProduct = 0.0;
    double norm1 = 0.0;
    double norm2 = 0.0;

    for (int i = 0; i < vector1.size(); i++) {
      dotProduct += vector1.get(i) * vector2.get(i);
      norm1 += vector1.get(i) * vector1.get(i);
      norm2 += vector2.get(i) * vector2.get(i);
    }

    if (norm1 == 0.0 || norm2 == 0.0) {
      return 0.0;
    }

    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
  }

  /** 从JSON字符串解析向量 */
  private List<Double> parseVectorFromJson(String vectorJson) {
    try {
      return objectMapper.readValue(vectorJson, new TypeReference<List<Double>>() {});
    } catch (Exception e) {
      log.debug("解析向量JSON失败: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /** 知识库条目与相似度的包装类 */
  private static class KnowledgeWithSimilarity {
    final KnowledgeBase knowledge;
    final double similarity;

    KnowledgeWithSimilarity(KnowledgeBase knowledge, double similarity) {
      this.knowledge = knowledge;
      this.similarity = similarity;
    }
  }
}

