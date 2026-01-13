import { createRouter, createWebHistory } from "vue-router";
import Home from "../views/Home.vue";
import { useUserStore } from "../stores/user";

const routes = [
  {
    path: "/",
    name: "Home",
    component: Home,
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("../views/Login.vue"),
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("../views/Register.vue"),
  },
  {
    path: "/diagnosis",
    name: "Diagnosis",
    component: () => import("../views/Diagnosis.vue"),
  },
  {
    path: "/history",
    name: "History",
    component: () => import("../views/History.vue"),
  },
  {
    path: "/profile",
    name: "Profile",
    component: () => import("../views/Profile.vue"),
  },
  {
    path: "/chat",
    name: "Chat",
    component: () => import("../views/Chat.vue"),
  },
  {
    path: "/chat/search",
    name: "ChatSearch",
    component: () => import("../views/ChatSearch.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const token = userStore.token || localStorage.getItem("token");

  // 公开路由，不需要登录
  const publicRoutes = ["/login", "/register"];

  if (publicRoutes.includes(to.path)) {
    // 如果已登录，跳转到首页
    if (token) {
      next("/");
    } else {
      next();
    }
  } else {
    // 需要登录的路由
    if (token) {
      next();
    } else {
      next("/login");
    }
  }
});

export default router;
