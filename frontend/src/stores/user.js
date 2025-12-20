import { defineStore } from "pinia";
import { ref } from "vue";

export const useUserStore = defineStore("user", () => {
  // 从 localStorage 恢复用户信息
  const savedUserInfo = localStorage.getItem("userInfo");
  const userInfo = ref(savedUserInfo ? JSON.parse(savedUserInfo) : null);
  const token = ref(localStorage.getItem("token") || "");

  const setUserInfo = (info) => {
    userInfo.value = info;
    // 持久化到 localStorage
    if (info) {
      localStorage.setItem("userInfo", JSON.stringify(info));
    } else {
      localStorage.removeItem("userInfo");
    }
  };

  const setToken = (newToken) => {
    token.value = newToken;
    localStorage.setItem("token", newToken);
  };

  const logout = () => {
    userInfo.value = null;
    token.value = "";
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");
    localStorage.removeItem("rememberedPhone");
    localStorage.removeItem("rememberedPassword");
  };

  return {
    userInfo,
    token,
    setUserInfo,
    setToken,
    logout,
  };
});
