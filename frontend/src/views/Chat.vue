<template>
  <div class="chat">
    <el-container class="chat-layout">
      <!-- 右侧聊天区域 -->
      <el-container class="chat-right-container">
        <!-- 头部 -->
        <el-header class="header-section">
          <div class="header-content">
            <div class="header-left">
              <el-button
                :icon="Menu"
                circle
                @click="sidebarVisible = true"
                class="menu-button"
              />
              <span class="header-title">AI智能助手</span>
            </div>
            <el-button @click="goBack" class="back-button"> 返回 </el-button>
          </div>
        </el-header>

        <!-- 聊天消息区域 -->
        <el-main class="chat-main" ref="chatMainRef">
          <div class="chat-messages" ref="messagesContainerRef">
            <!-- 空状态打招呼提示 -->
            <div v-if="messages.length === 0 && !isLoading" class="empty-state">
              <div class="empty-state-content">
                <div class="empty-state-icon">
                  <el-icon><Avatar /></el-icon>
                </div>
                <div class="empty-state-text">
                  您好！请问有什么可以帮助您的吗？
                </div>
              </div>
            </div>

            <!-- 消息列表 -->
            <div
              v-for="(message, index) in messages"
              :key="index"
              :ref="(el) => setMessageRef(el, index)"
              class="message-wrapper"
              :class="
                message.role === 'user'
                  ? 'message-user-wrapper'
                  : 'message-ai-wrapper'
              "
              :data-message-index="index"
            >
              <!-- AI消息：头像在左 -->
              <div class="message-avatar" v-if="message.role === 'assistant'">
                <el-icon><Avatar /></el-icon>
              </div>
              <!-- 用户消息：时间在左 -->
              <div class="message-time" v-if="message.role === 'user'">
                {{ formatTime(message.timestamp) }}
              </div>
              <!-- 消息气泡 -->
              <div
                class="message-bubble"
                :class="
                  message.role === 'user'
                    ? 'message-user-bubble'
                    : 'message-ai-bubble'
                "
              >
                <div
                  class="message-content"
                  v-html="formatMessage(message.content)"
                ></div>
              </div>
              <!-- 用户消息：头像在右 -->
              <div class="message-avatar" v-if="message.role === 'user'">
                <el-icon><User /></el-icon>
              </div>
              <!-- AI消息：时间在右 -->
              <div class="message-time" v-if="message.role === 'assistant'">
                {{ formatTime(message.timestamp) }}
              </div>
            </div>

            <!-- 加载指示器 -->
            <div v-if="isLoading" class="message-wrapper message-ai-wrapper">
              <div class="message-avatar">
                <el-icon><Avatar /></el-icon>
              </div>
              <div class="message-bubble message-ai-bubble">
                <div class="message-content">
                  <span class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </span>
                </div>
              </div>
              <div class="message-time">{{ formatTime(Date.now()) }}</div>
            </div>
          </div>
        </el-main>

        <!-- 底部输入区域 -->
        <el-footer class="chat-footer">
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              :maxlength="1000"
              placeholder="请输入您的问题..."
              class="chat-input"
              @keydown.enter.exact.prevent="handleSendMessage"
              @keydown.enter.shift.exact="inputMessage += '\n'"
              :disabled="isLoading"
              resize="none"
            />
            <div class="input-actions">
              <span class="char-count">{{ inputMessage.length }}/1000</span>
              <el-button
                type="primary"
                :loading="isLoading"
                :disabled="!inputMessage.trim() || isLoading"
                @click="handleSendMessage"
                class="send-button"
              >
                <el-icon v-if="!isLoading"><Promotion /></el-icon>
                <span>{{ isLoading ? "发送中..." : "发送" }}</span>
              </el-button>
            </div>
          </div>
        </el-footer>
      </el-container>

      <!-- 侧边栏抽屉 -->
      <el-drawer
        v-model="sidebarVisible"
        title="聊天历史"
        :with-header="true"
        :show-close="false"
        direction="ltr"
        size="280px"
      >
        <template #header>
          <div class="drawer-header">
            <div class="drawer-header-actions">
              <!-- <el-button
                :icon="Close"
                circle
                size="small"
                @click="sidebarVisible = false"
                class="drawer-action-button drawer-close-button"
              /> -->
            </div>
          </div>
        </template>
        <div class="sidebar-content">
          <el-button
            type="primary"
            class="new-chat-button"
            @click="handleSearch"
          >
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </el-button>
          <el-button
            type="primary"
            class="new-chat-button"
            @click="handleCreateNewChat"
          >
            新建对话
          </el-button>
          <div
            v-for="(session, index) in chatSessions"
            :key="session.id"
            class="session-item"
            :class="{ active: currentSessionId === session.id }"
            @click="
              switchSession(session.id);
              sidebarVisible = false;
            "
          >
            <el-tooltip
              :content="session.topic || '新对话'"
              placement="right"
              :show-after="0"
              :hide-after="0"
            >
              <div class="session-title">{{ session.topic || "新对话" }}</div>
            </el-tooltip>
            <div class="session-time">
              {{ formatSessionTime(session.updatedAt) }}
            </div>
          </div>
          <div v-if="chatSessions.length === 0" class="empty-sessions">
            暂无聊天记录
          </div>
        </div>
      </el-drawer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Avatar,
  User,
  Promotion,
  Plus,
  Menu,
  Close,
  Search,
} from "@element-plus/icons-vue";
import { aiChatApi } from "../api/chat";

const router = useRouter();
const route = useRoute();

const messages = ref([]);
const inputMessage = ref("");
const isLoading = ref(false);
const chatMainRef = ref(null);
const messagesContainerRef = ref(null);

// 聊天会话管理
const chatSessions = ref([]);
const currentSessionId = ref(null);
const sidebarVisible = ref(false);

// 消息引用映射
const messageRefs = ref({});

// 生成新的会话ID
const generateSessionId = () => {
  return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

// 创建新对话
const createNewChat = () => {
  const newSession = {
    id: generateSessionId(),
    topic: "新对话",
    updatedAt: Date.now(),
    messages: [],
  };
  chatSessions.value.unshift(newSession);
  currentSessionId.value = newSession.id;
  messages.value = [];
  // 保存到localStorage
  saveSessionsToLocal();
};

// 处理新建对话（从抽屉按钮）
const handleCreateNewChat = () => {
  createNewChat();
  sidebarVisible.value = false;
};

// 处理搜索
const handleSearch = () => {
  router.push({ name: "ChatSearch" });
};

// 切换会话
const switchSession = (sessionId) => {
  const session = chatSessions.value.find((s) => s.id === sessionId);
  if (session) {
    currentSessionId.value = sessionId;
    messages.value = session.messages || [];
    scrollToBottom();
  }
};

// 保存会话到localStorage
const saveSessionsToLocal = () => {
  try {
    localStorage.setItem("chatSessions", JSON.stringify(chatSessions.value));
  } catch (error) {
    console.error("保存会话失败:", error);
  }
};

// 从localStorage加载会话
const loadSessionsFromLocal = () => {
  try {
    const saved = localStorage.getItem("chatSessions");
    if (saved) {
      chatSessions.value = JSON.parse(saved);
      // 按更新时间排序
      chatSessions.value.sort((a, b) => b.updatedAt - a.updatedAt);
      // 如果有会话，切换到最新的
      if (chatSessions.value.length > 0) {
        currentSessionId.value = chatSessions.value[0].id;
        messages.value = chatSessions.value[0].messages || [];
      } else {
        createNewChat();
      }
    } else {
      createNewChat();
    }
  } catch (error) {
    console.error("加载会话失败:", error);
    createNewChat();
  }
};

// 更新当前会话
const updateCurrentSession = (topic, newMessages, shouldUpdateTime = true) => {
  const session = chatSessions.value.find(
    (s) => s.id === currentSessionId.value,
  );
  if (session) {
    if (topic) {
      session.topic = topic;
    }
    session.messages = newMessages;
    // 只有在发起对话时才更新时间
    if (shouldUpdateTime) {
      session.updatedAt = Date.now();
      // 重新排序
      chatSessions.value.sort((a, b) => b.updatedAt - a.updatedAt);
    }
    saveSessionsToLocal();
  }
};

// 格式化会话时间
const formatSessionTime = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return "刚刚";
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

// 格式化消息内容（支持换行）
const formatMessage = (content) => {
  if (!content) return "";
  return content
    .replace(/\n/g, "<br>")
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.*?)\*/g, "<em>$1</em>");
};

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${hours}:${minutes}`;
};

// 设置消息引用
const setMessageRef = (el, index) => {
  if (el) {
    messageRefs.value[index] = el;
  }
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainerRef.value) {
      messagesContainerRef.value.scrollTop =
        messagesContainerRef.value.scrollHeight;
    }
  });
};

// 滚动到指定消息
const scrollToMessage = (messageIndex) => {
  nextTick(() => {
    const messageEl = messageRefs.value[messageIndex];
    if (messageEl && messagesContainerRef.value) {
      const container = messagesContainerRef.value;
      const messageTop = messageEl.offsetTop;
      const containerHeight = container.clientHeight;
      const scrollTop =
        messageTop - containerHeight / 2 + messageEl.offsetHeight / 2;

      container.scrollTo({
        top: Math.max(0, scrollTop),
        behavior: "smooth",
      });

      // 高亮显示该消息
      messageEl.classList.add("message-highlight");
      setTimeout(() => {
        messageEl.classList.remove("message-highlight");
      }, 2000);
    }
  });
};

// 处理路由参数
const handleRouteParams = () => {
  const { sessionId, messageIndex } = route.query;

  if (sessionId) {
    // 切换到指定会话
    const session = chatSessions.value.find((s) => s.id === sessionId);
    if (session) {
      currentSessionId.value = sessionId;
      messages.value = session.messages || [];

      // 如果有消息索引，滚动到对应消息
      if (messageIndex !== undefined) {
        const index = parseInt(messageIndex);
        if (!isNaN(index) && index >= 0 && index < messages.value.length) {
          scrollToMessage(index);
        } else {
          scrollToBottom();
        }
      } else {
        scrollToBottom();
      }
    }
  }
};

// 发送消息
const handleSendMessage = async () => {
  const message = inputMessage.value.trim();
  if (!message || isLoading.value) return;

  // 如果没有当前会话，创建新会话
  if (!currentSessionId.value) {
    createNewChat();
  }

  // 添加用户消息
  messages.value.push({
    role: "user",
    content: message,
    timestamp: Date.now(),
  });

  inputMessage.value = "";
  scrollToBottom();

  isLoading.value = true;

  try {
    // 构建消息历史（只包含最近的6条消息，即3轮对话）
    // 每条消息明确标注发送者是"user"还是"assistant"
    const recentMessages = messages.value
      .slice(-6) // 只取最近6条
      .map((msg) => ({
        role: msg.role === "user" ? "user" : "assistant", // 明确标注发送者
        content: msg.content,
      }));

    // 添加系统提示词，要求AI返回话题主题
    const systemPrompt = {
      role: "system",
      content:
        "你是一位专业的AI医疗诊断助手，专注于脑干海绵状血管畸形的诊断咨询。请用专业但易懂的语言回答问题，提供准确的医疗建议。如果问题超出你的专业范围，请如实告知。\n\n" +
        "重要：请在每次回复的最后，以JSON格式返回本次对话的话题主题，格式如下：\n" +
        '{"topic": "话题主题，不超过20个字，要求非常简洁"}',
    };

    const chatMessages = [systemPrompt, ...recentMessages];

    // 发送消息并获取回复
    try {
      const result = await aiChatApi.sendMessage(chatMessages);
      // Result格式: { code: 200, data: { content: "...", topic: "..." }, message: "..." }
      if (result.code === 200) {
        const responseData = result.data || {};
        let content = "";
        let topic = null;

        // 处理新的响应格式（对象）
        if (typeof responseData === "object" && responseData !== null) {
          content = responseData.content || "";
          topic = responseData.topic || null;
        }
        // 兼容旧格式（字符串）
        else if (typeof result.data === "string") {
          content = result.data;
          // 尝试从内容中提取话题主题
          const topicMatch = content.match(/\{"topic":\s*"([^"]+)"\}/);
          if (topicMatch) {
            topic = topicMatch[1];
            // 移除JSON格式的话题主题
            content = content.replace(/\{"topic":\s*"[^"]+"\}\s*$/, "").trim();
          }
        }

        // 添加AI回复消息
        messages.value.push({
          role: "assistant",
          content: content,
          timestamp: Date.now(),
        });

        // 更新会话话题主题（如果是第一条AI回复，或者话题主题已变化）
        if (topic) {
          const session = chatSessions.value.find(
            (s) => s.id === currentSessionId.value,
          );
          if (session && (session.topic === "新对话" || !session.topic)) {
            updateCurrentSession(topic, messages.value);
          } else {
            updateCurrentSession(null, messages.value);
          }
        } else {
          updateCurrentSession(null, messages.value);
        }
      } else {
        // 添加错误消息
        messages.value.push({
          role: "assistant",
          content: `[错误: ${result.message || "未知错误"}]`,
          timestamp: Date.now(),
        });
        ElMessage.error(result.message || "AI回复失败");
        updateCurrentSession(null, messages.value);
      }
    } catch (error) {
      console.error("发送消息失败:", error);
      // 添加错误消息
      messages.value.push({
        role: "assistant",
        content: `[错误: ${error.message || "请求失败"}]`,
        timestamp: Date.now(),
      });
      ElMessage.error("发送失败，请稍后重试");
      updateCurrentSession(null, messages.value);
    }
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

// 返回
const goBack = () => {
  router.back();
};

onMounted(() => {
  loadSessionsFromLocal();

  // 处理路由参数（从搜索页面跳转过来）
  nextTick(() => {
    handleRouteParams();
    // 如果没有路由参数，滚动到底部
    if (!route.query.sessionId) {
      scrollToBottom();
    }
  });
});

// 监听路由变化
watch(
  () => route.query,
  () => {
    handleRouteParams();
  },
  { deep: true },
);

// 监听消息变化，自动保存（不更新时间，因为这只是加载历史消息）
watch(
  () => messages.value,
  (newMessages) => {
    if (currentSessionId.value && newMessages.length > 0) {
      updateCurrentSession(null, newMessages, false);
    }
  },
  { deep: true },
);
</script>

<style scoped>
.chat {
  height: 100vh;
  overflow: hidden;
  background: #ededed;
}

.chat-layout {
  height: 100vh;
  display: flex;
  flex-direction: row;
  background: #ededed;
  overflow: hidden;
}

/* 覆盖 Element Plus el-container 的默认样式 */
.chat-layout :deep(.el-container) {
  height: 100%;
  overflow: hidden;
}

/* 头部内容区域 */
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 12px;
  box-sizing: border-box;
}

/* 头部左侧区域 */
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.menu-button {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  margin-left: 3%;
}

.menu-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.back-button {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
  color: white !important;
  margin-right: 3%;
  border-radius: 6px !important;
  padding: 8px 16px !important;
  font-size: 14px !important;
  line-height: 1.5 !important;
  min-width: auto !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
  color: white !important;
}

.back-button:focus {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
  color: white !important;
}

/* 确保按钮文字显示 */
.back-button :deep(span) {
  color: white !important;
  display: inline-block !important;
}

/* 抽屉头部 */
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 0 !important;
}

/* 覆盖 Element Plus Drawer 的默认 header 样式 */
:deep(.el-drawer__header) {
  margin-bottom: 0 !important;
  padding-bottom: 0 !important;
}

/* 隐藏 Element Plus 默认的关闭按钮 */
:deep(.el-drawer__close-btn) {
  display: none !important;
}

/* 隐藏关闭图标 */
:deep(.el-drawer__header .el-icon) {
  display: none !important;
}

/* 自定义关闭按钮样式 */
.drawer-close-button {
  --el-button-hover-bg-color: var(--primary-gradient-subtle);
  --el-button-hover-border-color: var(--primary-color);
  --el-button-hover-text-color: var(--primary-color);
}

.drawer-header-actions {
  margin-right: 6%;
  display: flex;
  gap: 8px;
  align-items: center;
}

.drawer-action-button {
  --el-button-bg-color: transparent;
  --el-button-border-color: var(--border-color);
  --el-button-text-color: var(--text-primary);
  --el-button-hover-bg-color: var(--primary-gradient-subtle);
  --el-button-hover-border-color: var(--primary-color);
  --el-button-hover-text-color: var(--primary-color);

  background: var(--el-button-bg-color);
  border: 1px solid var(--el-button-border-color);
  color: var(--el-button-text-color);
  border-radius: 6px;
  padding: 6px 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.drawer-action-button:hover {
  background: var(--el-button-hover-bg-color);
  border-color: var(--el-button-hover-border-color);
  color: var(--el-button-hover-text-color);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(150, 120, 217, 0.2);
}

/* 自定义关闭按钮特殊样式 */
.drawer-close-button {
  --el-button-hover-bg-color: rgba(150, 120, 217, 0.15);
  --el-button-hover-border-color: var(--primary-color);
  --el-button-hover-text-color: var(--primary-color);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}

.sidebar-content > .new-chat-button {
  margin-left: 0;
  margin-right: 0;
}

.new-chat-button {
  width: 100% !important;
  margin: 0 !important;
  margin-bottom: 10px !important;
  padding: 8px 16px !important;
  font-size: 14px !important;
  font-weight: 500;
  display: flex !important;
  align-items: center;
  justify-content: center;
  gap: 6px;
  box-sizing: border-box;
  min-height: auto !important;
  height: auto !important;
}

.new-chat-button:last-of-type {
  margin-bottom: 10px !important;
}

.new-chat-button .el-icon {
  font-size: 14px;
  flex-shrink: 0;
  margin: 0 !important;
}

.new-chat-button span {
  flex-shrink: 0;
  margin: 0 !important;
}

.session-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.session-item:hover {
  background: var(--primary-gradient-subtle);
}

.session-item.active {
  background: var(--primary-gradient-subtle);
  border-left-color: var(--primary-color);
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.empty-sessions {
  padding: 24px 16px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}

/* 右侧聊天容器 */
.chat-right-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 头部 */
.header-section {
  background: var(--primary-color);
  color: white;
  padding: 0;
  box-shadow: 0 2px 8px rgba(150, 120, 217, 0.2);
  height: 60px;
  display: flex;
  align-items: center;
}

/* 聊天消息区域 */
.chat-main {
  flex: 1;
  padding: 10px 16px;
  overflow: hidden;
  background: #ededed;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 覆盖 Element Plus el-main 的默认样式 */
.chat-main :deep(.el-main) {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 10px;
  scroll-behavior: smooth;
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 空状态提示 - 居中显示 */
.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  max-width: 600px;
  padding: 0 20px;
  box-sizing: border-box;
}

.empty-state-content {
  text-align: center;
  animation: fadeIn 0.5s ease-in;
}

.empty-state-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: var(--primary-gradient-subtle);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(150, 120, 217, 0.15);
}

.empty-state-text {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 0 20px;
}

/* 消息包装器 - 微信风格 */
.message-wrapper {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
  animation: fadeIn 0.3s ease-in;
  position: relative;
  width: 100%;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-user-wrapper {
  justify-content: flex-end;
  flex-direction: row;
}

.message-user-wrapper .message-bubble {
  margin-left: 8px;
  margin-right: 8px;
}

.message-ai-wrapper {
  justify-content: flex-start;
  flex-direction: row;
}

.message-ai-wrapper .message-bubble {
  margin-left: 8px;
  margin-right: 8px;
}

/* 头像 - 微信风格圆形 */
.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
  margin: 0 8px;
  background: var(--primary-color);
  color: white;
}

.message-ai-wrapper .message-avatar {
  background: var(--primary-lighter);
  color: var(--primary-color);
}

.message-user-wrapper .message-avatar {
  background: var(--primary-color);
  color: white;
}

/* 消息气泡 - 微信风格 */
.message-bubble {
  max-width: 70%;
  min-width: 60px;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  padding: 10px 14px;
  border-radius: 6px;
}

/* 用户消息气泡 - 右侧紫色 */
.message-user-bubble {
  background: var(--primary-color);
  color: white;
  border-radius: 6px 6px 0 6px;
  box-shadow: 0 1px 2px rgba(150, 120, 217, 0.3);
}

/* AI消息气泡 - 左侧浅紫色 */
.message-ai-bubble {
  background: #f0e6ff;
  color: var(--text-primary);
  border-radius: 6px 6px 6px 0;
  border: 1px solid rgba(150, 120, 217, 0.15);
  box-shadow: 0 1px 2px rgba(150, 120, 217, 0.15);
}

/* 消息内容 */
.message-content {
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
}

.message-content :deep(strong) {
  font-weight: 600;
}

.message-content :deep(em) {
  font-style: italic;
}

.message-user-bubble .message-content {
  color: white;
}

.message-ai-bubble .message-content {
  color: var(--text-primary);
}

/* 消息高亮动画 */
.message-wrapper.message-highlight {
  animation: highlightMessage 2s ease-in-out;
}

@keyframes highlightMessage {
  0% {
    background: rgba(150, 120, 217, 0.3);
    transform: scale(1);
  }
  50% {
    background: rgba(150, 120, 217, 0.5);
    transform: scale(1.02);
  }
  100% {
    background: transparent;
    transform: scale(1);
  }
}

/* 时间戳 - 显示在消息旁边，微信风格 */
.message-time {
  font-size: 11px;
  color: #999;
  align-self: flex-end;
  margin: 0 4px;
  white-space: nowrap;
  padding-bottom: 4px;
  line-height: 1;
  flex-shrink: 0;
}

/* 打字指示器 - 紫色 */
.typing-indicator {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color);
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

/* 底部输入区域 - 微信风格，紫色主题 */
.chat-footer {
  background: #ededed;
  border-top: 1px solid rgba(150, 120, 217, 0.15);
  padding: 12px 16px;
  height: auto;
  box-shadow: 0 -1px 4px rgba(150, 120, 217, 0.08);
}

.input-container {
  max-width: 1200px;
  margin: 0 auto;
}

.chat-input {
  margin-bottom: 8px;
}

.chat-input :deep(.el-textarea__inner) {
  border: 1px solid rgba(150, 120, 217, 0.3);
  border-radius: 8px;
  font-size: 15px;
  padding: 12px;
  resize: none;
  background: white;
  color: var(--text-primary);
}

.chat-input :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(150, 120, 217, 0.1);
}

.chat-input :deep(.el-textarea__inner::placeholder) {
  color: #999;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 12px;
  color: var(--primary-color);
}

.send-button {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.send-button:hover {
  background: var(--primary-light);
  border-color: var(--primary-light);
}

.send-button:disabled {
  opacity: 0.5;
  background: var(--primary-color);
  border-color: var(--primary-color);
}

/* 滚动条样式 - 侧边栏 */
.sidebar-content::-webkit-scrollbar {
  width: 4px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: rgba(150, 120, 217, 0.3);
  border-radius: 2px;
}

.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: rgba(150, 120, 217, 0.5);
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .chat-main {
    padding: 8px 12px;
  }

  .message-bubble {
    max-width: 75%;
  }

  .message-avatar {
    width: 36px;
    height: 36px;
    font-size: 18px;
    margin: 0 6px;
  }

  .message-content {
    font-size: 14px;
  }

  .message-time {
    font-size: 10px;
  }

  .chat-footer {
    padding: 10px 12px;
  }

  .chat-input :deep(.el-textarea__inner) {
    font-size: 14px;
    padding: 10px;
  }

  .empty-state {
    max-width: 90%;
    padding: 0 16px;
  }

  .empty-state-icon {
    width: 64px;
    height: 64px;
    font-size: 32px;
    margin-bottom: 20px;
  }

  .empty-state-text {
    font-size: 14px;
    line-height: 1.6;
    padding: 0 10px;
  }
}

/* 滚动条样式 - 紫色 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(150, 120, 217, 0.3);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(150, 120, 217, 0.5);
}
</style>
