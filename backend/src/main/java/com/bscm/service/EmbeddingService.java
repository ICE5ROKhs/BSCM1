package com.bscm.service;

import java.util.List;

public interface EmbeddingService {
  /**
   * 将文本转换为向量
   *
   * @param text 要转换的文本
   * @return 向量数组（1536维）
   */
  List<Double> getEmbedding(String text);
}

