package com.bscm.controller;

import com.bscm.common.Result;
import com.bscm.logging.BusinessLogger;
import com.bscm.service.ChatService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

  private final ChatService chatService;
  private final BusinessLogger businessLogger;

  /** 发送聊天消息（流式响应） */
  @PostMapping("/stream")
  public SseEmitter sendMessageStream(@RequestBody ChatRequest request) {
    SseEmitter emitter = new SseEmitter(120000L); // 120秒超时

    try {
      if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
        emitter.completeWithError(new IllegalArgumentException("消息不能为空"));
        return emitter;
      }

      // 构建消息列表
      List<Map<String, String>> messages = request.getMessages();
      log.info("收到聊天请求，消息数: {}", messages.size());

      // 异步处理流式响应（Spring会自动提交响应头）
      new Thread(
              () -> {
                boolean completed = false;
                try {
                  log.info("开始处理AI聊天流");
                  chatService.sendMessageStream(
                      messages,
                      chunk -> {
                        try {
                          log.debug("发送chunk: {}", chunk);
                          emitter.send(SseEmitter.event().data(chunk));
                        } catch (IOException e) {
                          log.warn("SSE连接已关闭或客户端断开连接", e);
                          // 抛出运行时异常以中断流处理
                          throw new RuntimeException("SSE连接已关闭", e);
                        }
                      });
                  // 正常完成
                  log.info("AI服务处理完成，准备关闭SSE流");
                  emitter.complete();
                  completed = true;
                  log.info("SSE流正常完成");
                  businessLogger.logBusinessEvent("AI聊天完成", "消息数: " + messages.size());
                } catch (Exception e) {
                  log.error("处理聊天消息失败", e);
                  businessLogger.logBusinessError("AI聊天失败", "消息数: " + messages.size(), e);
                  if (!completed) {
                    try {
                      // 发送错误信息给前端
                      String errorMessage =
                          e.getMessage() != null ? e.getMessage() : "AI服务暂时不可用，请稍后重试";
                      log.info("发送错误信息给前端: {}", errorMessage);
                      emitter.send(SseEmitter.event().data("ERROR:" + errorMessage));
                      emitter.complete();
                      completed = true;
                    } catch (Exception sendException) {
                      log.error("发送错误信息失败", sendException);
                      try {
                        if (!completed) {
                          emitter.completeWithError(sendException);
                          completed = true;
                        }
                      } catch (Exception completeException) {
                        log.debug("完成SSE连接时出错（可能连接已关闭）", completeException);
                      }
                    }
                  }
                }
              })
          .start();

      return emitter;
    } catch (Exception e) {
      log.error("创建SSE流失败", e);
      emitter.completeWithError(e);
      return emitter;
    }
  }

  /** 发送聊天消息（非流式响应） */
  @PostMapping("/message")
  public Result<String> sendMessage(@RequestBody ChatRequest request) {
    try {
      if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
        return Result.error("消息不能为空");
      }

      List<Map<String, String>> messages = request.getMessages();
      String response = chatService.sendMessage(messages);

      businessLogger.logBusinessEvent("AI聊天完成", "消息数: " + messages.size());
      return Result.success(response);
    } catch (Exception e) {
      log.error("发送聊天消息失败", e);
      businessLogger.logBusinessError("AI聊天失败", "", e);
      return Result.error("发送消息失败: " + e.getMessage());
    }
  }

  @Data
  static class ChatRequest {
    private List<Map<String, String>> messages;
  }
}
