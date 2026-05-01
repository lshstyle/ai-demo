import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tokenStorage } from '@/utils/request'
import type { LoginResult } from '@/api/auth'
import * as authApi from '@/api/auth'

const USER_KEY = 'ai_demo_user_info'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(tokenStorage.get())
  const userInfo = ref<Partial<LoginResult> | null>(
    (() => {
      const s = localStorage.getItem(USER_KEY)
      return s ? JSON.parse(s) : null
    })()
  )

  function setSession(result: LoginResult) {
    token.value = result.accessToken
    userInfo.value = result
    tokenStorage.set(result.accessToken)
    localStorage.setItem(USER_KEY, JSON.stringify(result))
  }

  function clearSession() {
    token.value = null
    userInfo.value = null
    tokenStorage.clear()
    localStorage.removeItem(USER_KEY)
  }

  async function loginByPassword(username: string, password: string, remember = false) {
    const res = await authApi.loginByPassword({ username, password, remember })
    setSession(res)
    return res
  }

  async function loginByPhone(phone: string, code: string) {
    const res = await authApi.loginByPhone({ phone, code })
    setSession(res)
    return res
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // ignore
    } finally {
      clearSession()
    }
  }

  return { token, userInfo, setSession, clearSession, loginByPassword, loginByPhone, logout }
})
