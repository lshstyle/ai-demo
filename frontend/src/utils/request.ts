import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 后端统一响应结构
 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const TOKEN_KEY = 'ai_demo_access_token'

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY)
}

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStorage.get()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

service.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const body = response.data
    if (body && body.code === 200) {
      return body as unknown as AxiosResponse
    }
    ElMessage.error(body?.message || '请求失败')
    return Promise.reject(body)
  },
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      tokenStorage.clear()
      ElMessage.error('登录已过期，请重新登录')
      if (location.pathname !== '/login') {
        location.href = '/login'
      }
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

/**
 * 统一 request：返回 data 字段
 */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config).then((res) => (res as unknown as ApiResult<T>).data)
}

export default service
