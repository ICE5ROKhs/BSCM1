<template>
  <div class="home">
    <el-container class="home-layout">
      <el-header class="header-section">
        <div class="header-content">
          <div class="logo-section">
            <div class="logo-icon">BSCM</div>
            <h1>AI智能诊断系统</h1>
          </div>
          <div class="header-subtitle">
            基于先进AI技术，提供专业医疗诊断服务
          </div>
        </div>
      </el-header>
      <el-main class="main-section">
        <!-- 主页内容 -->
        <div v-if="activeTab === 'home'" class="home-content">
          <div class="hero-section">
            <div class="hero-content">
              <h2 class="hero-title">智能医疗诊断，让健康触手可及</h2>
              <p class="hero-description">
                采用最新的人工智能技术，结合专业医疗知识库，为您提供准确、快速的诊断建议
              </p>
              <el-button
                type="primary"
                size="large"
                class="hero-button"
                @click="goToDiagnosis"
              >
                <el-icon><Document /></el-icon>
                立即开始诊断
              </el-button>
            </div>
          </div>
        </div>
        <!-- 个人页面内容 -->
        <ProfileTab v-if="activeTab === 'profile'" />
      </el-main>
      <!-- 底部导航栏 -->
      <el-footer class="bottom-nav">
        <div class="nav-container">
          <div
            class="nav-item"
            :class="{ active: activeTab === 'home' }"
            @click="activeTab = 'home'"
          >
            <el-icon class="nav-icon"><House /></el-icon>
            <span class="nav-label">主页</span>
          </div>
          <div
            class="nav-item"
            :class="{ active: activeTab === 'profile' }"
            @click="activeTab = 'profile'"
          >
            <el-icon class="nav-icon"><User /></el-icon>
            <span class="nav-label">个人</span>
          </div>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Document, House, User } from "@element-plus/icons-vue";
import ProfileTab from "./ProfileTab.vue";

const router = useRouter();
const activeTab = ref("home");

const goToDiagnosis = () => {
  router.push("/diagnosis");
};
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: var(--background-color);
}

.home-layout {
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
  height: 100%;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 12px;
  text-align: center;
  position: relative;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.header-content h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 500;
  letter-spacing: 0;
}

.header-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 6px;
  font-weight: 400;
}

.header-actions {
  position: absolute;
  top: 16px;
  right: 24px;
}

.logout-button {
  border-color: rgba(255, 255, 255, 0.5);
  color: white;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.logout-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.8);
  color: white;
}

/* 主内容区域 */
.main-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 80px 24px;
  width: 100%;
  box-sizing: border-box;
}

/* Hero区域 */
.hero-section {
  background: white;
  border-radius: 8px;
  padding: 48px 32px;
  margin-bottom: 40px;
  border: 1px solid var(--border-color);
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 32px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 16px;
  line-height: 1.4;
}

.hero-description {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 32px;
}

.hero-button {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.hero-button:hover {
  background-color: var(--primary-dark);
}

/* 底部导航栏 */
.bottom-nav {
  background: white;
  border-top: 1px solid var(--border-color);
  padding: 0;
  height: 60px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 8px 24px;
  border-radius: 8px;
  transition: all 0.3s ease;
  color: var(--text-secondary);
  flex: 1;
  max-width: 200px;
}

.nav-item:hover {
  background-color: rgba(64, 158, 255, 0.1);
  color: var(--primary-color);
}

.nav-item.active {
  color: var(--primary-color);
}

.nav-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.nav-label {
  font-size: 12px;
  font-weight: 500;
}

/* ========== 响应式 ========== */
@media screen and (max-width: 1024px) {
  .main-section {
    padding: 32px 20px 80px 20px;
  }

  .hero-title {
    font-size: 28px;
  }
}

@media screen and (max-width: 768px) {
  .header-content {
    padding: 0px 0px;
  }

  .header-content h1 {
    font-size: 22px;
  }

  .header-subtitle {
    font-size: 13px;
  }

  .header-actions {
    top: 12px;
    right: 12px;
  }

  .logout-button {
    padding: 6px 12px;
    font-size: 12px;
  }

  .logo-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }

  .main-section {
    padding: 24px 16px 80px 16px;
  }

  .hero-section {
    padding: 32px 20px;
    border-radius: 12px;
  }

  .hero-title {
    font-size: 24px;
  }

  .hero-description {
    font-size: 14px;
  }

  .hero-button {
    width: 100%;
  }

  .nav-icon {
    font-size: 22px;
  }

  .nav-label {
    font-size: 11px;
  }

  .nav-item {
    padding: 6px 16px;
  }
}
</style>
