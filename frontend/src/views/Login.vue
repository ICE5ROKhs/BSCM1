<template>
  <div class="login">
    <div class="login-container">
      <div class="login-form">
        <h2 class="form-title">登录</h2>

        <!-- 标签页切换登录方式 -->
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="账密登录" name="password">
            <el-form
              :model="loginForm"
              :rules="loginRules"
              ref="loginFormRef"
              @submit.prevent="handleLogin"
            >
              <el-form-item prop="phone">
                <el-input
                  v-model="loginForm.phone"
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

              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  clearable
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <div class="form-options">
                <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
                <a
                  href="#"
                  class="forgot-password-link"
                  @click.prevent="showForgotPasswordDialog = true"
                  >忘记密码？</a
                >
              </div>

              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  style="width: 100%"
                  :loading="loading"
                  @click="handleLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="验证码登录" name="code">
            <el-form
              :model="codeLoginForm"
              :rules="codeLoginRules"
              ref="codeLoginFormRef"
              @submit.prevent="handleCodeLogin"
            >
              <el-form-item prop="phone">
                <el-input
                  v-model="codeLoginForm.phone"
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
                  v-model="codeLoginForm.verificationCode"
                  placeholder="请输入验证码"
                  size="large"
                  maxlength="6"
                  clearable
                  @keyup.enter="handleCodeLogin"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                  <template #suffix>
                    <el-button
                      link
                      type="primary"
                      :disabled="codeCountdown > 0"
                      @click="sendCode('code')"
                    >
                      {{
                        codeCountdown > 0
                          ? `${codeCountdown}秒后重试`
                          : "获取验证码"
                      }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  style="width: 100%"
                  :loading="loading"
                  @click="handleCodeLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="form-footer">
          <span
            >还没有账号？<a href="#" @click.prevent="goToRegister"
              >立即注册</a
            ></span
          >
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="showForgotPasswordDialog"
      title="忘记密码"
      :width="dialogWidth"
      :close-on-click-modal="false"
    >
      <el-form
        :model="forgotPasswordForm"
        :rules="forgotPasswordRules"
        ref="forgotPasswordFormRef"
        label-width="100px"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="forgotPasswordForm.phone"
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

        <el-form-item label="验证码" prop="verificationCode">
          <el-input
            v-model="forgotPasswordForm.verificationCode"
            placeholder="请输入验证码"
            size="large"
            maxlength="6"
            clearable
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
            <template #suffix>
              <el-button
                link
                type="primary"
                :disabled="forgotPasswordCountdown > 0"
                @click="sendCode('forgot')"
              >
                {{
                  forgotPasswordCountdown > 0
                    ? `${forgotPasswordCountdown}秒后重试`
                    : "获取验证码"
                }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="forgotPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showForgotPasswordDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="forgotPasswordLoading"
          @click="handleResetPassword"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { authApi } from "../api/auth";
import { Phone, Lock, Message } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const rememberPassword = ref(false);
const activeTab = ref("password");
const codeCountdown = ref(0);
const forgotPasswordCountdown = ref(0);
const showForgotPasswordDialog = ref(false);
const forgotPasswordLoading = ref(false);

// 响应式对话框宽度
const dialogWidth = computed(() => {
  return window.innerWidth <= 768 ? "90%" : "400px";
});

const loginForm = reactive({
  phone: "",
  password: "",
});

const codeLoginForm = reactive({
  phone: "",
  verificationCode: "",
});

const forgotPasswordForm = reactive({
  phone: "",
  verificationCode: "",
  newPassword: "",
  confirmPassword: "",
});

const loginFormRef = ref(null);
const codeLoginFormRef = ref(null);
const forgotPasswordFormRef = ref(null);

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请再次输入新密码"));
  } else if (value !== forgotPasswordForm.newPassword) {
    callback(new Error("两次输入密码不一致"));
  } else {
    callback();
  }
};

const loginRules = {
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
};

const codeLoginRules = {
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
};

const forgotPasswordRules = {
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
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
};

onMounted(() => {
  // 加载记住的密码
  const rememberedPhone = localStorage.getItem("rememberedPhone");
  const rememberedPassword = localStorage.getItem("rememberedPassword");
  if (rememberedPhone && rememberedPassword) {
    loginForm.phone = rememberedPhone;
    loginForm.password = rememberedPassword;
    rememberPassword.value = true;
  }
});

// 发送验证码
const sendCode = async (type) => {
  let phone = "";
  if (type === "code") {
    phone = codeLoginForm.phone;
  } else if (type === "forgot") {
    phone = forgotPasswordForm.phone;
  }

  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.error("请输入正确的手机号");
    return;
  }

  try {
    await authApi.sendCode(phone);
    ElMessage.success("验证码已发送");

    // 开始倒计时
    if (type === "code") {
      codeCountdown.value = 60;
      const timer = setInterval(() => {
        codeCountdown.value--;
        if (codeCountdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    } else if (type === "forgot") {
      forgotPasswordCountdown.value = 60;
      const timer = setInterval(() => {
        forgotPasswordCountdown.value--;
        if (forgotPasswordCountdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "发送验证码失败");
  }
};

// 账密登录
const handleLogin = async () => {
  await loginFormRef.value?.validate();

  loading.value = true;
  try {
    const response = await authApi.login(loginForm.phone, loginForm.password);
    const { token, user } = response.data.data;

    userStore.setToken(token);
    userStore.setUserInfo(user);

    // 记住密码
    if (rememberPassword.value) {
      localStorage.setItem("rememberedPhone", loginForm.phone);
      localStorage.setItem("rememberedPassword", loginForm.password);
    } else {
      localStorage.removeItem("rememberedPhone");
      localStorage.removeItem("rememberedPassword");
    }

    ElMessage.success("登录成功");
    router.push("/");
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "登录失败");
  } finally {
    loading.value = false;
  }
};

// 验证码登录
const handleCodeLogin = async () => {
  await codeLoginFormRef.value?.validate();

  loading.value = true;
  try {
    const response = await authApi.quickLogin(
      codeLoginForm.phone,
      codeLoginForm.verificationCode,
    );
    const { token, user } = response.data.data;

    userStore.setToken(token);
    userStore.setUserInfo(user);

    ElMessage.success("登录成功");
    router.push("/");
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "登录失败");
  } finally {
    loading.value = false;
  }
};

// 重置密码
const handleResetPassword = async () => {
  await forgotPasswordFormRef.value?.validate();

  forgotPasswordLoading.value = true;
  try {
    await authApi.resetPassword(
      forgotPasswordForm.phone,
      forgotPasswordForm.newPassword,
      forgotPasswordForm.verificationCode,
    );

    ElMessage.success("密码重置成功，请使用新密码登录");
    showForgotPasswordDialog.value = false;
    // 清空表单
    forgotPasswordForm.phone = "";
    forgotPasswordForm.verificationCode = "";
    forgotPasswordForm.newPassword = "";
    forgotPasswordForm.confirmPassword = "";
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "重置密码失败");
  } finally {
    forgotPasswordLoading.value = false;
  }
};

const goToRegister = () => {
  router.push("/register");
};
</script>

<style scoped>
.login {
  min-height: 100vh;
  background: var(--background-color);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-form {
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

.login-tabs {
  margin-bottom: 20px;
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-password-link {
  color: var(--primary-color);
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
}

.forgot-password-link:hover {
  text-decoration: underline;
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
  .login {
    padding: 16px;
    align-items: flex-start;
    padding-top: 20px;
  }

  .login-form {
    padding: 24px 20px;
    border-radius: 12px;
  }

  .form-title {
    font-size: 20px;
    margin-bottom: 24px;
  }

  .form-options {
    font-size: 14px;
  }
}
</style>
