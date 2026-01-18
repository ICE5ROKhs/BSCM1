package com.bscm.service;

import java.util.List;
import java.util.Map;

public interface RagService {
  /**
   * 根据用户问题检索相似知识并构建增强提示词
   *
   * @param userQuestion 用户问题
   * @param conversationHistory 对话历史
   * @return 增强后的提示词（包含检索到的知识）
   */
  String buildEnhancedPrompt(String userQuestion, List<Map<String, String>> conversationHistory);
}

