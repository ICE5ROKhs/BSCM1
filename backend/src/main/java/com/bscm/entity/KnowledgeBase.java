package com.bscm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "knowledge_base")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "question", columnDefinition = "TEXT", nullable = false)
  private String question;

  @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
  private String answer;

  @Column(name = "type", nullable = false)
  private Integer type; // 1=基础知识, 2=实际病例

  @Column(name = "vector", columnDefinition = "TEXT")
  private String vector; // 存储向量数据（JSON格式，1536维向量数组）

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}

