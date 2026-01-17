<template>
  <div class="knowledge-base">
    <el-container class="knowledge-layout">
      <el-header class="header-section">
        <div class="header-content">
          <div class="header-left">
            <el-button :icon="ArrowLeft" circle @click="goBack" class="back-button" />
            <span class="header-title">RAG知识库</span>
          </div>
        </div>
      </el-header>

      <el-main class="main-section">
        <!-- 标签页切换 -->
        <el-tabs v-model="activeTab" class="knowledge-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="基础知识" name="basic">
            <div class="tab-content">
              <!-- 搜索框 -->
              <div class="search-section">
                <div class="search-container">
                  <el-input
                    v-model="basicSearchKeyword"
                    placeholder="搜索问题..."
                    clearable
                    @input="handleBasicSearch"
                    class="search-input"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-checkbox
                    v-model="basicSearchInAnswer"
                    @change="handleBasicSearch"
                    class="search-checkbox"
                  >
                    同时搜索答案
                  </el-checkbox>
                </div>
              </div>

              <!-- 知识列表 -->
              <div class="knowledge-list" v-loading="basicLoading">
                <div
                  v-for="item in basicKnowledgeList"
                  :key="item.id"
                  class="knowledge-item"
                >
                  <div class="knowledge-question">
                    <el-icon class="question-icon"><QuestionFilled /></el-icon>
                    <span>{{ item.question }}</span>
                  </div>
                  <div class="knowledge-answer">
                    <el-icon class="answer-icon"><ChatLineRound /></el-icon>
                    <div class="answer-content">{{ item.answer }}</div>
                  </div>
                </div>

                <!-- 空状态 -->
                <div v-if="!basicLoading && basicKnowledgeList.length === 0" class="empty-state">
                  <el-icon class="empty-icon"><DocumentDelete /></el-icon>
                  <p class="empty-text">暂无相关知识</p>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="实际病例" name="case">
            <div class="tab-content">
              <!-- 搜索框 -->
              <div class="search-section">
                <div class="search-container">
                  <el-input
                    v-model="caseSearchKeyword"
                    placeholder="搜索问题..."
                    clearable
                    @input="handleCaseSearch"
                    class="search-input"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-checkbox
                    v-model="caseSearchInAnswer"
                    @change="handleCaseSearch"
                    class="search-checkbox"
                  >
                    同时搜索答案
                  </el-checkbox>
                </div>
              </div>

              <!-- 知识列表 -->
              <div class="knowledge-list" v-loading="caseLoading">
                <div
                  v-for="item in caseKnowledgeList"
                  :key="item.id"
                  class="knowledge-item"
                >
                  <div class="knowledge-question">
                    <el-icon class="question-icon"><QuestionFilled /></el-icon>
                    <span>{{ item.question }}</span>
                  </div>
                  <div class="knowledge-answer">
                    <el-icon class="answer-icon"><ChatLineRound /></el-icon>
                    <div class="answer-content">{{ item.answer }}</div>
                  </div>
                </div>

                <!-- 空状态 -->
                <div v-if="!caseLoading && caseKnowledgeList.length === 0" class="empty-state">
                  <el-icon class="empty-icon"><DocumentDelete /></el-icon>
                  <p class="empty-text">暂无相关知识</p>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  ArrowLeft,
  Search,
  QuestionFilled,
  ChatLineRound,
  DocumentDelete,
} from "@element-plus/icons-vue";
import { knowledgeApi } from "../api/knowledge";

const router = useRouter();

const activeTab = ref("basic");
const basicSearchKeyword = ref("");
const caseSearchKeyword = ref("");
const basicSearchInAnswer = ref(false);
const caseSearchInAnswer = ref(false);
const basicKnowledgeList = ref([]);
const caseKnowledgeList = ref([]);
const basicLoading = ref(false);
const caseLoading = ref(false);

// 搜索防抖
let basicSearchTimer = null;
let caseSearchTimer = null;

// 加载基础知识
const loadBasicKnowledge = async (keyword = null, searchInAnswer = false) => {
  basicLoading.value = true;
  try {
    const result = await knowledgeApi.getKnowledgeList(1, keyword, searchInAnswer);
    if (result.code === 200) {
      basicKnowledgeList.value = result.data || [];
    } else {
      ElMessage.error(result.message || "获取基础知识失败");
      basicKnowledgeList.value = [];
    }
  } catch (error) {
    console.error("加载基础知识失败:", error);
    ElMessage.error("加载基础知识失败，请稍后重试");
    basicKnowledgeList.value = [];
  } finally {
    basicLoading.value = false;
  }
};

// 加载实际病例
const loadCaseKnowledge = async (keyword = null, searchInAnswer = false) => {
  caseLoading.value = true;
  try {
    const result = await knowledgeApi.getKnowledgeList(2, keyword, searchInAnswer);
    if (result.code === 200) {
      caseKnowledgeList.value = result.data || [];
    } else {
      ElMessage.error(result.message || "获取实际病例失败");
      caseKnowledgeList.value = [];
    }
  } catch (error) {
    console.error("加载实际病例失败:", error);
    ElMessage.error("加载实际病例失败，请稍后重试");
    caseKnowledgeList.value = [];
  } finally {
    caseLoading.value = false;
  }
};

// 处理基础知识搜索
const handleBasicSearch = () => {
  if (basicSearchTimer) {
    clearTimeout(basicSearchTimer);
  }
  basicSearchTimer = setTimeout(() => {
    loadBasicKnowledge(
      basicSearchKeyword.value && basicSearchKeyword.value.trim()
        ? basicSearchKeyword.value.trim()
        : null,
      basicSearchInAnswer.value
    );
  }, 300);
};

// 处理实际病例搜索
const handleCaseSearch = () => {
  if (caseSearchTimer) {
    clearTimeout(caseSearchTimer);
  }
  caseSearchTimer = setTimeout(() => {
    loadCaseKnowledge(
      caseSearchKeyword.value && caseSearchKeyword.value.trim()
        ? caseSearchKeyword.value.trim()
        : null,
      caseSearchInAnswer.value
    );
  }, 300);
};

// 返回
const goBack = () => {
  router.back();
};

// 监听标签页切换
const handleTabChange = (tabName) => {
  if (tabName === "basic" && basicKnowledgeList.value.length === 0) {
    loadBasicKnowledge();
  } else if (tabName === "case" && caseKnowledgeList.value.length === 0) {
    loadCaseKnowledge();
  }
};

onMounted(() => {
  // 默认加载基础知识
  loadBasicKnowledge();
});
</script>

<style scoped>
.knowledge-base {
  min-height: 100vh;
  background: var(--background-color);
}

.knowledge-layout {
  min-height: 100vh;
  background: var(--background-color);
  display: flex;
  flex-direction: column;
}

/* 头部区域 */
.header-section {
  background: var(--primary-gradient);
  color: white;
  padding: 0;
  box-shadow: 0 2px 12px var(--shadow-color);
  height: 60px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 20px;
  font-weight: 500;
  color: white;
}

.back-button {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 主内容区域 */
.main-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  width: 100%;
  box-sizing: border-box;
  flex: 1;
}

/* 标签页样式 */
.knowledge-tabs {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px var(--shadow-color);
}

.tab-content {
  padding-top: 20px;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 24px;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 300px;
  max-width: 600px;
}

.search-checkbox {
  white-space: nowrap;
  flex-shrink: 0;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

/* 知识列表 */
.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.knowledge-item {
  background: var(--background-light);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.knowledge-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px var(--shadow-color);
}

.knowledge-question {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.question-icon {
  color: var(--primary-color);
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.knowledge-answer {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.answer-icon {
  color: var(--primary-light);
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 4px;
}

.answer-content {
  flex: 1;
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 64px;
  color: var(--border-color);
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  margin: 0;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .main-section {
    padding: 16px;
  }

  .knowledge-tabs {
    padding: 16px;
  }

  .knowledge-item {
    padding: 16px;
  }

  .knowledge-question {
    font-size: 15px;
  }

  .answer-content {
    font-size: 13px;
  }
}
</style>

