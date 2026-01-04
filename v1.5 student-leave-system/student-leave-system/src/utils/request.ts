// src/utils/request.ts
import axios, {
  type AxiosInstance,
  type InternalAxiosRequestConfig,
  type AxiosResponse,
  type AxiosRequestConfig
} from 'axios';
import { ElMessage } from 'element-plus';

interface ApiResult<T = any> {
  code: number;
  message: string;
  data: T;
}

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
});

// 请求拦截器：注入 token
service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一解包 Result
service.interceptors.response.use(
  (raw: AxiosResponse<ApiResult<any>>) => {
    const res = raw.data;

    // 标准 Result
    if (res && typeof res === 'object' && 'code' in res && 'message' in res && 'data' in res) {
      if (res.code === 0) return res.data;
      ElMessage.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || `业务错误(code=${res.code})`));
    }

    // 非标准：直接返回 body（不要 return raw，否则 TS 又回到 AxiosResponse）
    return raw.data;
  },
  (error) => {
    console.error('Response Err:', error);

    const status = error?.response?.status;
    const serverMsg = error?.response?.data?.message || error?.response?.data?.msg;

    let message = serverMsg || error?.message || '网络错误';

    if (status === 401) {
      message = serverMsg || '未登录或登录已过期';
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('displayName');
      localStorage.removeItem('roleCode');
    }

    ElMessage.error(message);
    return Promise.reject(error);
  }
);

// ✅ 关键：让 TS 认为 request.get/post 返回的是「解包后的业务数据」
const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get<any, T>(url, config);
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post<any, T>(url, data, config);
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put<any, T>(url, data, config);
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete<any, T>(url, config);
  }
};

export default request;
export { service };
