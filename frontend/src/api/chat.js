import axios from "axios";

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
    return config;
  },
  (error) => {
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
