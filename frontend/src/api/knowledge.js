import { createApiInstance } from "../utils/api";
import { API_CONFIG } from "../config/api";

const api = createApiInstance(API_CONFIG.timeout);

export const knowledgeApi = {
  // 获取知识列表
  getKnowledgeList: async (type, keyword = null, searchInAnswer = false) => {
    try {
      const params = { type };
      if (keyword) {
        params.keyword = keyword;
      }
      // 始终传递 searchInAnswer 参数，确保后端能正确判断
      params.searchInAnswer = searchInAnswer;
      const response = await api.get("/api/knowledge/list", { params });
      return response.data;
    } catch (error) {
      console.error("获取知识列表API错误:", error);
      throw error;
    }
  },
};

