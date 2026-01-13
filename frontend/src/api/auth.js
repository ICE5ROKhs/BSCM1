import { createApiInstance } from "../utils/api";

const api = createApiInstance();

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
