import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'

// Mock request & API（避免真实 HTTP 请求）
vi.mock('@/utils/request', () => {
  let token: string | null = null
  return {
    tokenStorage: {
      get: () => token,
      set: (v: string) => {
        token = v
      },
      clear: () => {
        token = null
      }
    },
    request: vi.fn()
  }
})

vi.mock('@/api/auth', () => ({
  loginByPassword: vi.fn(),
  loginByPhone: vi.fn(),
  logout: vi.fn(() => Promise.resolve())
}))

import PasswordLoginForm from '@/views/login/components/PasswordLoginForm.vue'
import ElementPlus from 'element-plus'

describe('PasswordLoginForm.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  function buildRouter() {
    return createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: { template: '<div>home</div>' } },
        { path: '/login', component: { template: '<div>login</div>' } }
      ]
    })
  }

  it('默认值应为 admin / 123456', () => {
    const router = buildRouter()
    const wrapper = mount(PasswordLoginForm, {
      global: { plugins: [ElementPlus, router] }
    })
    const inputs = wrapper.findAll('input')
    expect((inputs[0].element as HTMLInputElement).value).toBe('admin')
    expect((inputs[1].element as HTMLInputElement).value).toBe('123456')
  })

  it('应渲染登录按钮', () => {
    const router = buildRouter()
    const wrapper = mount(PasswordLoginForm, {
      global: { plugins: [ElementPlus, router] }
    })
    const buttons = wrapper.findAll('button')
    const loginBtn = buttons.find((b: any) => /登\s*录/.test(b.text()))
    expect(loginBtn).toBeTruthy()
  })

  it('应包含 "忘记密码" 链接和记住我复选', () => {
    const router = buildRouter()
    const html = mount(PasswordLoginForm, {
      global: { plugins: [ElementPlus, router] }
    }).html()
    expect(html).toContain('忘记密码')
    expect(html).toContain('记住我')
  })
})
