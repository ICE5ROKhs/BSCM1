package com.bscm.controller;

import com.bscm.common.Result;
import com.bscm.entity.KnowledgeBase;
import com.bscm.service.KnowledgeBaseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class KnowledgeBaseController {

  private final KnowledgeBaseService knowledgeBaseService;

  /** 根据类型获取知识列表 */
  @GetMapping("/list")
  public Result<List<KnowledgeBase>> getKnowledgeList(
      @RequestParam Integer type,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean searchInAnswer) {
    try {
      List<KnowledgeBase> knowledgeList;
      if (keyword != null && !keyword.trim().isEmpty()) {
        // 如果 searchInAnswer 为 null，默认为 false（只搜索问题）
        boolean shouldSearchInAnswer = searchInAnswer != null && searchInAnswer;
        knowledgeList =
            knowledgeBaseService.searchKnowledgeByTypeAndKeyword(
                type, keyword, shouldSearchInAnswer);
      } else {
        knowledgeList = knowledgeBaseService.getKnowledgeByType(type);
      }
      return Result.success(knowledgeList);
    } catch (Exception e) {
      log.error("获取知识列表失败", e);
      return Result.error("获取知识列表失败: " + e.getMessage());
    }
  }
}

