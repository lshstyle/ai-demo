import { request } from '@/utils/request'

export interface LoginPayload {
  username: string
  password: string
  remember?: boolean
}

export interface PhoneLoginPayload {
  phone: string
  code: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  userId: number
  username: string
  nickname?: string
  avatarUrl?: string
}

/** 账号密码登录 */
export function loginByPassword(payload: LoginPayload) {
  return request<LoginResult>({
    url: '/auth/login',
    method: 'POST',
    data: payload
  })
}

/** 手机号验证码登录 */
export function loginByPhone(payload: PhoneLoginPayload) {
  return request<LoginResult>({
    url: '/auth/login/phone',
    method: 'POST',
    data: payload
  })
}

/** 发送短信验证码 */
export function sendSmsCode(phone: string) {
  return request<void>({
    url: '/sms/send',
    method: 'POST',
    params: { phone }
  })
}

/** 退出登录 */
export function logout() {
  return request<void>({
    url: '/auth/logout',
    method: 'POST'
  })
}
