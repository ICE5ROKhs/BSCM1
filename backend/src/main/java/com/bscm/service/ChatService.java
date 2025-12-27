package com.bscm.service;

import java.util.List;
import java.util.Map;

public interface ChatService {
  /**
   * 发送聊天消息并获取AI回复（非流式）
   *
   * @param messages 消息列表
   * @return AI回复内容
   */
  String sendMessage(List<Map<String, String>> messages);

  /**
   * 发送聊天消息并获取AI回复（流式）
   *
   * @param messages 消息列表
   * @param onChunk 处理每个数据块的回调函数
   */
  void sendMessageStream(List<Map<String, String>> messages, ChunkHandler onChunk);

  /** 处理流式响应数据块的接口 */
  @FunctionalInterface
  interface ChunkHandler {
    void handle(String chunk);
  }
}
