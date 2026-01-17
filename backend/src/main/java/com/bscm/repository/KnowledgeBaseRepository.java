package com.bscm.repository;

import com.bscm.entity.KnowledgeBase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

  /** 根据类型查询知识，按问题长度排序 */
  @Query("SELECT k FROM KnowledgeBase k WHERE k.type = :type ORDER BY LENGTH(k.question) ASC")
  List<KnowledgeBase> findByTypeOrderByQuestionLengthAsc(Integer type);

  /** 根据类型和问题关键字搜索（只搜索问题） */
  @Query(
      "SELECT k FROM KnowledgeBase k WHERE k.type = :type AND "
          + "LOWER(k.question) LIKE LOWER(CONCAT('%', :keyword, '%')) "
          + "ORDER BY LENGTH(k.question) ASC")
  List<KnowledgeBase> findByTypeAndQuestionKeyword(
      @Param("type") Integer type, @Param("keyword") String keyword);

  /** 根据类型和关键字搜索（同时搜索问题和答案） */
  @Query(
      "SELECT k FROM KnowledgeBase k WHERE k.type = :type AND "
          + "(LOWER(k.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "LOWER(k.answer) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
          + "ORDER BY LENGTH(k.question) ASC")
  List<KnowledgeBase> findByTypeAndKeyword(
      @Param("type") Integer type, @Param("keyword") String keyword);
}

