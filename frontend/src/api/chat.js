import { createApiInstance } from "../utils/api";
import { API_CONFIG } from "../config/api";

// 使用后端API，AI响应可能较慢，设置120秒超时
const api = createApiInstance(API_CONFIG.timeoutLong);

export const aiChatApi = {
  // 发送聊天消息（非流式响应）
  sendMessage: async (messages) => {
    try {
      const response = await api.post("/chat/message", { messages });
      return response.data;
    } catch (error) {
      console.error("AI聊天API错误:", error);
      throw error;
    }
  },
};
