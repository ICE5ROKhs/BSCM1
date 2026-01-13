// API 配置
// 在 Android 应用中，需要使用完整 URL 而不是相对路径

// 检测是否为 Capacitor 环境
function isCapacitor() {
  return (
    typeof window !== "undefined" &&
    (window.Capacitor ||
      window.cordova ||
      /capacitor/i.test(navigator.userAgent))
  );
}

// 获取 API 基础地址
// 优先级：环境变量 > localStorage > 默认值
export function getApiBaseURL() {
  // 如果是移动应用环境（Capacitor）
  if (isCapacitor()) {
    // 从环境变量或配置获取，如果没有则使用默认值
    // 注意：在生产环境中，你需要将后端 API 地址配置在这里
    const apiUrl =
      import.meta.env.VITE_API_BASE_URL ||
      localStorage.getItem("API_BASE_URL") ||
      "http://your-backend-server.com";
    return apiUrl.replace(/\/$/, ""); // 移除末尾的斜杠
  }

  // Web 环境使用相对路径（通过代理）
  return "/api";
}

export const API_CONFIG = {
  baseURL: getApiBaseURL(),
  timeout: 30000,
  timeoutLong: 120000, // AI 响应可能需要更长时间
};
