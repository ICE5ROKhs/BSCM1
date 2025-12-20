import axios from "axios";
import logger from "../utils/logger";

const api = axios.create({
  baseURL: "/api",
  timeout: 30000,
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
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export const authApi = {
  // 发送验证码
  sendCode: async (phone) => {
    return await api.post("/auth/send-code", { phone });
  },

  // 用户注册
  register: async (phone, password, verificationCode) => {
    return await api.post("/auth/register", {
      phone,
      password,
      verificationCode,
    });
  },

  // 用户登录
  login: async (phone, password) => {
    return await api.post("/auth/login", {
      phone,
      password,
    });
  },

  // 验证码登录
  quickLogin: async (phone, verificationCode) => {
    return await api.post("/auth/quick-login", {
      phone,
      verificationCode,
    });
  },

  // 重置密码（忘记密码）
  resetPassword: async (phone, newPassword, verificationCode) => {
    return await api.post("/auth/reset-password", {
      phone,
      newPassword,
      verificationCode,
    });
  },
};
