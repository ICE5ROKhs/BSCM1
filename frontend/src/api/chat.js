import axios from "axios";
import logger from "../utils/logger";

// 使用后端API，通过代理访问
const api = axios.create({
  baseURL: "/api",
  timeout: 120000, // AI响应可能较慢，设置120秒超时
});

// 添加请求拦截器：添加token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // 记录API请求
    logger.logApiRequest(config);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// 响应拦截器：处理token过期和日志记录
api.interceptors.response.use(
  (response) => {
    // 记录API响应
    logger.logApiResponse(response);
    return response;
  },
  (error) => {
    // 记录API错误
    logger.logApiError(error);

    if (error.response?.status === 401) {
      logger.warn("用户未授权，清除token并跳转到登录页");
      localStorage.removeItem("token");
      localStorage.removeItem("rememberedPhone");
      // 延迟跳转，避免在聊天页面立即跳转
      setTimeout(() => {
        window.location.href = "/login";
      }, 1000);
    }
    return Promise.reject(error);
  },
);

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
