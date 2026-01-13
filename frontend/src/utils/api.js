import axios from "axios";
import logger from "./logger";
import { getApiBaseURL, API_CONFIG } from "../config/api";

// 创建 API 实例
export function createApiInstance(timeout = API_CONFIG.timeout) {
  const api = axios.create({
    baseURL: getApiBaseURL(),
    timeout: timeout,
  });

  // 请求拦截器：添加token和日志记录
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
      logger.error("API请求拦截器错误", error);
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

  return api;
}
