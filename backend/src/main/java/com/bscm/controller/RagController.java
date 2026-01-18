package com.bscm.controller;

import com.bscm.common.Result;
import com.bscm.service.RagService;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RagController {

  private final RagService ragService;

  @PostMapping("/enhanced-prompt")
  public Result<String> getEnhancedPrompt(@RequestBody RagRequest request) {
    try {
      if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
        return Result.error("问题不能为空");
      }

      String userQuestion = request.getQuestion().trim();
      List<Map<String, String>> conversationHistory = request.getConversationHistory();

      log.info("========================================");
      log.info("【RAG增强提示词生成】");
      log.info("用户问题: {}", userQuestion);
      log.info("----------------------------------------");

      String enhancedPrompt = ragService.buildEnhancedPrompt(userQuestion, conversationHistory);

      log.info("【生成的RAG增强提示词】");
      log.info("----------------------------------------");
      log.info("{}", enhancedPrompt);
      log.info("========================================");

      return Result.success(enhancedPrompt);
    } catch (Exception e) {
      log.error("生成RAG增强提示词失败", e);
      return Result.error("生成提示词失败: " + e.getMessage());
    }
  }

  @Data
  static class RagRequest {
    private String question;
    private List<Map<String, String>> conversationHistory;
  }
}

