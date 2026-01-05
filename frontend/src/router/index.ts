// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';

// === 1. 导入所有页面组件 ===

// 🎓 学生端
import Timetable from '../views/student/Timetable.vue';
import LeaveList from '../views/student/LeaveList.vue';
import LeaveApply from '../views/student/LeaveApply.vue';
import CheckIn from '../views/student/CheckIn.vue'; // 课堂签到

// 📋 辅导员端
import TodoList from '../views/tutor/TodoList.vue';
import Statistics from '../views/counselor/Statistics.vue'; // 班级统计
import PublicLeave from '../views/counselor/PublicLeave.vue'; // 发起公假 (已修正为 .vue)

// 👨‍🏫 任课老师端
import LeaveConfirm from '../views/teacher/LeaveConfirm.vue';
import AttendanceCheck from '../views/teacher/AttendanceCheck.vue'; // 考勤登记
import AttendanceExport from '../views/teacher/AttendanceExport.vue';
// ⚙️ 管理员端
import TimetableManage from '../views/admin/TimetableManage.vue';
import SystemSettings from '../views/admin/SystemSettings.vue';

// 🔐 登录页
import Login from '../views/Login.vue'; // 👈 新增：系统设置 (需确保你已创建该文件)

const routes = [
  // 🔐 登录
  { path: '/login', component: Login },

  // 默认跳转到学生课表
  { path: '/', redirect: '/student/timetable' },

  // --- 🎓 学生路由 ---
  { path: '/student/timetable', component: Timetable },
  { path: '/leave/list', component: LeaveList },
  { path: '/leave/apply', component: LeaveApply },
  { path: '/student/checkin', component: CheckIn },

  // --- 📋 辅导员路由 ---
  { path: '/tutor/todos', component: TodoList },
  { path: '/counselor/statistics', component: Statistics },
  { path: '/counselor/public-leave', component: PublicLeave },

  // --- 👨‍🏫 老师路由 ---
  { path: '/teacher/confirm', component: LeaveConfirm },
  { path: '/teacher/attendance', component: AttendanceCheck },
  { path: '/teacher/export', component: AttendanceExport },

  // --- ⚙️ 管理员路由 ---
  { path: '/admin/timetable', component: TimetableManage },
  { path: '/admin/settings', component: SystemSettings }, // 👈 这一行就是修复“点击系统设置跳回首页”的关键

  // 404 捕获 (防止乱输地址白屏)
  { path: '/:pathMatch(.*)*', redirect: '/' }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});


// 🔒 路由守卫：未登录先去 /login
router.beforeEach((to) => {
  const token = localStorage.getItem('token');

  // 未登录：放行登录页，其余全部跳转
  if (!token && to.path !== '/login') return '/login';

  // 已登录：不允许回到登录页
  if (token && to.path === '/login') return '/';

  // 已登录访问根路径：按角色跳到各自首页（避免一律跳学生页）
  if (token && to.path === '/') {
    const rc = (localStorage.getItem('roleCode') || '').toUpperCase();
    if (rc === 'ADMIN') return '/admin/timetable';
    if (rc === 'COUNSELOR') return '/tutor/todos';
    if (rc === 'TEACHER') return '/teacher/confirm';
    // STUDENT 或未知
    return '/student/timetable';
  }

  return true;
});


export default router;