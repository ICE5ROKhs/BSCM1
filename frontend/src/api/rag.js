import { createApiInstance } from "../utils/api";
import { API_CONFIG } from "../config/api";

const api = createApiInstance(API_CONFIG.timeoutLong);

export const ragApi = {
  /**
   * 获取RAG增强后的提示词（仅用于控制台输出，不用于实际AI调用）
   * @param {string} question - 用户问题
   * @param {Array} conversationHistory - 对话历史
   * @returns {Promise} API响应
   */
  getEnhancedPrompt: async (question, conversationHistory = []) => {
    try {
      const response = await api.post("/api/rag/enhanced-prompt", {
        question,
        conversationHistory,
      });
      return response.data;
    } catch (error) {
      console.error("RAG增强提示词API错误:", error);
      throw error;
    }
  },
};

