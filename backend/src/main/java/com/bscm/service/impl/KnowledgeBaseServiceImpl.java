package com.bscm.service.impl;

import com.bscm.entity.KnowledgeBase;
import com.bscm.repository.KnowledgeBaseRepository;
import com.bscm.service.KnowledgeBaseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

  private final KnowledgeBaseRepository knowledgeBaseRepository;

  @Override
  public List<KnowledgeBase> getKnowledgeByType(Integer type) {
    log.info("查询知识库，类型: {}", type);
    return knowledgeBaseRepository.findByTypeOrderByQuestionLengthAsc(type);
  }

  @Override
  public List<KnowledgeBase> searchKnowledgeByTypeAndKeyword(
      Integer type, String keyword, Boolean searchInAnswer) {
    log.info("搜索知识库，类型: {}, 关键字: {}, 搜索答案: {}", type, keyword, searchInAnswer);
    if (keyword == null || keyword.trim().isEmpty()) {
      return getKnowledgeByType(type);
    }
    String trimmedKeyword = keyword.trim();
    // 明确判断：只有当 searchInAnswer 明确为 true 时才搜索答案
    // 如果为 null 或 false，都只搜索问题
    boolean shouldSearchInAnswer = Boolean.TRUE.equals(searchInAnswer);
    if (shouldSearchInAnswer) {
      // 同时搜索问题和答案
      log.debug("使用同时搜索问题和答案的方法");
      return knowledgeBaseRepository.findByTypeAndKeyword(type, trimmedKeyword);
    } else {
      // 只搜索问题
      log.debug("使用只搜索问题的方法");
      return knowledgeBaseRepository.findByTypeAndQuestionKeyword(type, trimmedKeyword);
    }
  }
}

