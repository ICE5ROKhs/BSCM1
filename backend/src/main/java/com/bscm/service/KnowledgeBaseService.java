package com.bscm.service;

import com.bscm.entity.KnowledgeBase;
import java.util.List;

public interface KnowledgeBaseService {
  /** 根据类型查询知识列表 */
  List<KnowledgeBase> getKnowledgeByType(Integer type);

  /** 根据类型和关键字搜索知识 */
  List<KnowledgeBase> searchKnowledgeByTypeAndKeyword(
      Integer type, String keyword, Boolean searchInAnswer);
}

