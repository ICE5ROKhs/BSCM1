<template>
  <div class="chat-search">
    <el-container class="search-layout">
      <!-- 头部 -->
      <el-header class="search-header">
        <div class="header-content">
          <div class="header-left">
            <el-button
              :icon="ArrowLeft"
              circle
              @click="goBack"
              class="back-button"
            />
            <span class="header-title">搜索聊天记录</span>
          </div>
        </div>
      </el-header>

      <!-- 搜索区域 -->
      <el-main class="search-main">
        <div class="search-container">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索聊天主题或内容..."
            class="search-input"
            clearable
            @input="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <!-- 搜索结果 -->
          <div v-if="searchResults.length > 0" class="results-container">
            <div class="results-header">
              <span class="results-count"
                >找到 {{ searchResults.length }} 条结果</span
              >
            </div>
            <div class="results-list">
              <div
                v-for="(result, index) in searchResults"
                :key="index"
                class="result-item"
                @click="handleResultClick(result)"
              >
                <div class="result-header">
                  <span class="result-topic">{{
                    result.session.topic || "新对话"
                  }}</span>
                  <span class="result-time">{{
                    formatSessionTime(result.session.updatedAt)
                  }}</span>
                </div>
                <div class="result-content">
                  <div
                    class="result-message"
                    v-html="
                      highlightKeyword(result.message.content, searchKeyword)
                    "
                  ></div>
                </div>
                <div class="result-meta">
                  <span class="result-role">
                    {{ result.message.role === "user" ? "我" : "AI助手" }}
                  </span>
                  <span class="result-timestamp">
                    {{ formatTime(result.message.timestamp) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div
            v-else-if="hasSearched && searchKeyword.trim()"
            class="empty-results"
          >
            <div class="empty-icon">
              <el-icon><Search /></el-icon>
            </div>
            <div class="empty-text">未找到相关结果</div>
          </div>

          <!-- 初始状态提示 -->
          <div v-else class="empty-state">
            <div class="empty-icon">
              <el-icon><Search /></el-icon>
            </div>
            <div class="empty-text">输入关键词搜索聊天记录</div>
            <div class="empty-hint">可以搜索聊天主题或聊天内容</div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ArrowLeft, Search } from "@element-plus/icons-vue";

const router = useRouter();

const searchKeyword = ref("");
const searchResults = ref([]);
const hasSearched = ref(false);

// 加载所有聊天会话
const loadAllSessions = () => {
  try {
    const saved = localStorage.getItem("chatSessions");
    if (saved) {
      return JSON.parse(saved);
    }
  } catch (error) {
    console.error("加载会话失败:", error);
  }
  return [];
};

// 执行搜索
const handleSearch = () => {
  const keyword = searchKeyword.value.trim();
  if (!keyword) {
    searchResults.value = [];
    hasSearched.value = false;
    return;
  }

  hasSearched.value = true;
  const sessions = loadAllSessions();
  const results = [];

  // 遍历所有会话
  sessions.forEach((session) => {
    const messages = session.messages || [];
    const matchedMessageIndices = new Set();

    // 检查主题是否匹配
    if (
      session.topic &&
      session.topic.toLowerCase().includes(keyword.toLowerCase())
    ) {
      // 如果主题匹配，添加第一条消息作为结果
      if (messages.length > 0) {
        results.push({
          session: session,
          message: messages[0],
          matchType: "topic",
          messageIndex: 0,
        });
        matchedMessageIndices.add(0);
      }
    }

    // 检查消息内容是否匹配
    messages.forEach((message, index) => {
      if (
        message.content &&
        message.content.toLowerCase().includes(keyword.toLowerCase())
      ) {
        // 如果主题已匹配且这是第一条消息，跳过重复添加
        if (!matchedMessageIndices.has(index)) {
          results.push({
            session: session,
            message: message,
            matchType: "content",
            messageIndex: index,
          });
          matchedMessageIndices.add(index);
        }
      }
    });
  });

  // 按会话更新时间排序，最新的在前
  results.sort((a, b) => {
    return b.session.updatedAt - a.session.updatedAt;
  });

  searchResults.value = results;
};

// 高亮关键词
const highlightKeyword = (text, keyword) => {
  if (!text || !keyword) return text;
  const regex = new RegExp(`(${keyword})`, "gi");
  return text.replace(regex, '<mark class="highlight">$1</mark>');
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

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${hours}:${minutes}`;
};

// 处理搜索结果点击
const handleResultClick = (result) => {
  // 跳转到聊天页面，并传递会话ID和消息索引
  const session = result.session;
  const messageIndex =
    result.messageIndex !== undefined
      ? result.messageIndex
      : (session.messages || []).findIndex((msg) => msg === result.message);

  // 通过路由参数传递信息
  router.push({
    name: "Chat",
    query: {
      sessionId: session.id,
      messageIndex: messageIndex >= 0 ? messageIndex : 0,
    },
  });
};

// 返回
const goBack = () => {
  router.back();
};

onMounted(() => {
  // 可以在这里添加一些初始化逻辑
});
</script>

<style scoped>
.chat-search {
  min-height: 100vh;
  background: #ededed;
}

.search-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ededed;
}

/* 头部 */
.search-header {
  background: var(--primary-color);
  color: white;
  padding: 0;
  box-shadow: 0 2px 8px rgba(150, 120, 217, 0.2);
  height: 60px;
  display: flex;
  align-items: center;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 12px;
  box-sizing: border-box;
}

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

.back-button {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  margin-left: 3%;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 搜索区域 */
.search-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #ededed;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
}

.search-input {
  margin-bottom: 20px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: transparent !important;
  box-shadow: none !important;
  border: 2px solid rgba(150, 120, 217, 0.3);
  border-radius: 12px;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(150, 120, 217, 0.1) !important;
}

.search-input :deep(.el-input__inner) {
  height: 48px;
  font-size: 16px;
  border-radius: 12px;
  border: none;
  background-color: transparent;
  padding-left: 3%;
}

.search-input :deep(.el-input__inner:focus) {
  border: none;
  box-shadow: none;
}

.search-input :deep(.el-input__prefix) {
  left: 12px;
  color: var(--primary-color);
  font-size: 20px;
}

/* 搜索结果 */
.results-container {
  margin-top: 20px;
}

.results-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(150, 120, 217, 0.2);
}

.results-count {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.result-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(150, 120, 217, 0.15);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.result-topic {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 12px;
  flex-shrink: 0;
}

.result-content {
  margin-bottom: 12px;
}

.result-message {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-message :deep(.highlight) {
  background: #fff3cd;
  color: var(--primary-color);
  font-weight: 600;
  padding: 2px 4px;
  border-radius: 3px;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(150, 120, 217, 0.1);
}

.result-role {
  font-size: 12px;
  color: var(--primary-color);
  font-weight: 500;
}

.result-timestamp {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 空状态 */
.empty-results,
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
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

.empty-text {
  font-size: 16px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .search-main {
    padding: 16px 12px;
  }

  .search-input :deep(.el-input__inner) {
    height: 44px;
    font-size: 15px;
  }

  .result-item {
    padding: 12px;
  }

  .result-topic {
    font-size: 15px;
  }

  .result-message {
    font-size: 13px;
  }

  .empty-icon {
    width: 64px;
    height: 64px;
    font-size: 32px;
    margin-bottom: 20px;
  }

  .empty-text {
    font-size: 14px;
  }

  .empty-hint {
    font-size: 13px;
  }
}

/* 滚动条样式 */
.search-main::-webkit-scrollbar {
  width: 6px;
}

.search-main::-webkit-scrollbar-track {
  background: transparent;
}

.search-main::-webkit-scrollbar-thumb {
  background: rgba(150, 120, 217, 0.3);
  border-radius: 3px;
}

.search-main::-webkit-scrollbar-thumb:hover {
  background: rgba(150, 120, 217, 0.5);
}
</style>
