<template>
  <div class="profile">
    <el-container class="profile-layout">
      <el-header class="header-section">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600 mr-3">个人中心</span>
          </template>
        </el-page-header>
      </el-header>
      <el-main>
        <el-card>
          <el-form :model="userInfo" :label-width="labelWidth">
            <el-form-item label="用户名">
              <el-input
                v-model="userInfo.username"
                placeholder="请输入用户名"
              />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input
                v-model="userInfo.phone"
                type="tel"
                placeholder="请输入手机号"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存</el-button>
              <el-button
                type="danger"
                @click="handleLogout"
                style="margin-left: 10px"
                >退出登录</el-button
              >
            </el-form-item>
          </el-form>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const userStore = useUserStore();
const userInfo = ref({
  username: "",
  phone: "",
});

// 响应式标签宽度
const labelWidth = computed(() => {
  return window.innerWidth <= 768 ? "80px" : "120px";
});

onMounted(() => {
  // 加载用户信息
  if (userStore.userInfo) {
    userInfo.value = {
      username: userStore.userInfo.username || "",
      phone: userStore.userInfo.phone || "",
    };
  }
});

const saveProfile = () => {
  ElMessage.success("保存成功");
};

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

const goBack = () => {
  router.push("/");
};
</script>

<style scoped>
.profile {
  min-height: 100vh;
}

.profile-layout {
  min-height: 100vh;
}

.el-header {
  background: var(--primary-gradient);
  color: white;
  border-bottom: none;
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px var(--shadow-color);
}

.el-main {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.el-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: none;
}

.el-card:hover {
  border-color: var(--border-color);
  box-shadow: 0 2px 8px var(--shadow-color);
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .el-header {
    padding: 0 16px;
  }

  .el-main {
    padding: 16px;
  }

  .el-card {
    border-radius: 12px;
  }
}

@media screen and (min-width: 1200px) {
  .el-main {
    padding: 24px 32px;
  }
}
</style>
