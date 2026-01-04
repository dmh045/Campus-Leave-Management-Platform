<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 p-6">
    <div class="w-full max-w-md bg-white rounded-2xl shadow-lg border border-gray-200 p-8">
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">登录</h1>
        <p class="text-sm text-gray-500 mt-1">请输入账号密码登录系统（学生/辅导员/老师/管理员）。</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" class="w-full">
            <el-option label="学生" value="student" />
            <el-option label="辅导员" value="counselor" />
            <el-option label="任课老师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
          <div class="text-xs text-gray-400 mt-2">
          </div>
        </el-form-item>

        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="学生填学号；教职工填工号" autocomplete="username" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password autocomplete="current-password" />
        </el-form-item>

        <el-button type="primary" class="w-full" :loading="loading" @click="onSubmit">
          登录
        </el-button>

      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { roleMenus } from '@/config/menu';

type RolePick = 'student' | 'counselor' | 'teacher' | 'admin';

const router = useRouter();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  role: (localStorage.getItem('last_login_role') as RolePick) || 'student',
  username: '',
  password: ''
});

const rules: FormRules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const loginTypeByRole = (role: RolePick) => (role === 'student' ? 'STUDENT' : 'STAFF');

const onSubmit = async () => {
  const ok = await formRef.value?.validate().catch(() => false);
  if (!ok) return;

  loading.value = true;
  try {
    localStorage.setItem('last_login_role', form.role);

    const resp = await userStore.login({
      username: form.username.trim(),
      password: form.password,
      loginType: loginTypeByRole(form.role)
    });

    // 如果用户选择的角色与后端实际返回不一致，给个提示（不阻塞）
    const expected =
      form.role === 'student' ? 'STUDENT' :
      form.role === 'counselor' ? 'COUNSELOR' :
      form.role === 'teacher' ? 'TEACHER' :
      form.role === 'admin' ? 'ADMIN' : '';

    const actual = (resp.roleCode || '').toUpperCase();
    if (expected && actual && expected !== actual) {
      ElMessage.warning(`后端返回角色为 ${actual}，已按后端权限进入（你选择的是 ${expected}）。`);
    } else {
      ElMessage.success('登录成功');
    }

    // 跳转到该角色的默认首页
    const role = userStore.currentRole;
    const first = roleMenus[role]?.[0]?.path || '/student/timetable';
    router.replace(first);
  } finally {
    loading.value = false;
  }
};
</script>
