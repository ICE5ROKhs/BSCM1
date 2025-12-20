<template>
  <div class="profile-tab">
    <div class="profile-content">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>个人基本信息</span>
          </div>
        </template>

        <div class="user-info">
          <div class="info-item">
            <div class="info-label">用户名</div>
            <div class="info-value">{{ userInfo.username || "未设置" }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">手机号</div>
            <div class="info-value">{{ userInfo.phone || "未设置" }}</div>
          </div>
        </div>

        <div class="logout-section">
          <el-button
            type="danger"
            size="large"
            class="logout-button"
            @click="handleLogout"
          >
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { SwitchButton } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const userStore = useUserStore();

// 使用 computed 来响应式地获取用户信息
const userInfo = computed(() => {
  return (
    userStore.userInfo || {
      username: "",
      phone: "",
    }
  );
});

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("确定要退出登录吗？", "确认退出", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    userStore.logout();
    ElMessage.success("已退出登录");
    router.push("/login");
  } catch (error) {
    if (error !== "cancel") {
      console.error(error);
    }
  }
};
</script>

<style scoped>
.profile-tab {
  min-height: calc(100vh - 200px);
  padding-bottom: 80px;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.profile-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: none;
}

.profile-card:hover {
  border-color: var(--border-color);
  box-shadow: 0 2px 8px var(--shadow-color);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-info {
  margin-bottom: 32px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  width: 100px;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.info-value {
  flex: 1;
  font-size: 16px;
  color: var(--text-primary);
}

.logout-section {
  margin-top: 32px;
  text-align: center;
}

.logout-button {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 6px;
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .profile-content {
    padding: 16px;
  }

  .profile-card {
    border-radius: 12px;
  }

  .info-label {
    width: 80px;
    font-size: 14px;
  }

  .info-value {
    font-size: 14px;
  }
}
</style>
