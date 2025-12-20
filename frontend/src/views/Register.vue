<template>
  <div class="register">
    <div class="register-container">
      <div class="register-form">
        <h2 class="form-title">注册</h2>
        <el-form
          :model="registerForm"
          :rules="registerRules"
          ref="registerFormRef"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              size="large"
              maxlength="11"
              clearable
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="verificationCode">
            <el-input
              v-model="registerForm.verificationCode"
              placeholder="请输入验证码"
              size="large"
              maxlength="6"
              clearable
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
            <el-button
              link
              type="primary"
              :disabled="codeCountdown > 0"
              @click="sendCode"
              style="margin-top: 8px; padding: 0"
            >
              {{
                codeCountdown > 0 ? `${codeCountdown}秒后重试` : "获取验证码"
              }}
            </el-button>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              clearable
              @keyup.enter="handleRegister"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span
              >已有账号？<a href="#" @click.prevent="goToLogin"
                >立即登录</a
              ></span
            >
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { authApi } from "../api/auth";
import { Phone, Lock, Message } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const codeCountdown = ref(0);

const registerForm = reactive({
  phone: "",
  verificationCode: "",
  password: "",
  confirmPassword: "",
});

const registerFormRef = ref(null);

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const registerRules = {
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
  verificationCode: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { pattern: /^\d{6}$/, message: "请输入6位验证码", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
};

// 发送验证码
const sendCode = async () => {
  if (!registerForm.phone || !/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.error("请输入正确的手机号");
    return;
  }

  try {
    await authApi.sendCode(registerForm.phone);
    ElMessage.success("验证码已发送");

    // 开始倒计时
    codeCountdown.value = 60;
    const timer = setInterval(() => {
      codeCountdown.value--;
      if (codeCountdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "发送验证码失败");
  }
};

// 注册
const handleRegister = async () => {
  await registerFormRef.value?.validate();

  loading.value = true;
  try {
    const response = await authApi.register(
      registerForm.phone,
      registerForm.password,
      registerForm.verificationCode,
    );
    const { token, user } = response.data.data;

    userStore.setToken(token);
    userStore.setUserInfo(user);

    ElMessage.success("注册成功");
    router.push("/");
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "注册失败");
  } finally {
    loading.value = false;
  }
};

const goToLogin = () => {
  router.push("/login");
};
</script>

<style scoped>
.register {
  min-height: 100vh;
  background: var(--background-color);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 400px;
}

.register-form {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.form-title {
  font-size: 24px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 30px;
  text-align: center;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--text-secondary);
}

.form-footer a {
  color: var(--primary-color);
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* ========== 响应式 ========== */
@media screen and (max-width: 768px) {
  .register {
    padding: 16px;
    align-items: flex-start;
    padding-top: 20px;
  }

  .register-form {
    padding: 24px 20px;
    border-radius: 12px;
  }

  .form-title {
    font-size: 20px;
    margin-bottom: 24px;
  }
}
</style>
