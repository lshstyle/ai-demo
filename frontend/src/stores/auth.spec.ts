import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock API 模块
vi.mock('@/api/auth', () => ({
  loginByPassword: vi.fn(),
  loginByPhone: vi.fn(),
  logout: vi.fn(() => Promise.resolve())
}))

// Mock request.ts（只需要 tokenStorage）
vi.mock('@/utils/request', () => {
  let store: string | null = null
  return {
    tokenStorage: {
      get: () => store,
      set: (v: string) => {
        store = v
      },
      clear: () => {
        store = null
      }
    },
    request: vi.fn()
  }
})

import * as authApi from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

describe('stores/auth', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('loginByPassword 成功后应写入 token 与用户信息', async () => {
    const mock = authApi.loginByPassword as unknown as ReturnType<typeof vi.fn>
    mock.mockResolvedValue({
      accessToken: 'at-token',
      refreshToken: 'rt-token',
      userId: 1,
      username: 'admin',
      nickname: '管理员',
      avatarUrl: ''
    })

    const store = useAuthStore()
    await store.loginByPassword('admin', '123456', false)

    expect(store.token).toBe('at-token')
    expect(store.userInfo?.username).toBe('admin')
    expect(localStorage.getItem('ai_demo_user_info')).toContain('admin')
  })

  it('logout 应清空 session', async () => {
    const store = useAuthStore()
    store.setSession({
      accessToken: 'x',
      refreshToken: 'y',
      userId: 1,
      username: 'u'
    })
    expect(store.token).toBe('x')

    await store.logout()

    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('ai_demo_user_info')).toBeNull()
  })

  it('loginByPhone 失败时应抛异常且不写入 session', async () => {
    const mock = authApi.loginByPhone as unknown as ReturnType<typeof vi.fn>
    mock.mockRejectedValue(new Error('验证码错误'))

    const store = useAuthStore()
    await expect(store.loginByPhone('13800000000', '000000')).rejects.toThrow('验证码错误')
    expect(store.token).toBeNull()
  })
})
