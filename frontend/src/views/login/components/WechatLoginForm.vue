<template>
  <div class="wechat-login">
    <div class="qr-box">
      <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="微信扫码" class="qr-img" />
      <div v-else class="qr-placeholder">
        <el-icon :size="60" class="is-loading"><Loading /></el-icon>
        <div class="qr-tip">二维码加载中...</div>
      </div>

      <div v-if="status === 'EXPIRED'" class="qr-mask">
        <div>二维码已过期</div>
        <el-button type="primary" size="small" @click="refresh">点击刷新</el-button>
      </div>
      <div v-else-if="status === 'CONFIRMED'" class="qr-mask success">
        <el-icon :size="48"><CircleCheck /></el-icon>
        <div>登录成功，正在跳转...</div>
      </div>
    </div>
    <p class="hint">{{ hintText }}</p>
    <p class="sub-hint">演示模式：打开二维码 5 秒后自动完成扫码登录</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElIcon, ElButton } from 'element-plus'
import { Loading, CircleCheck } from '@element-plus/icons-vue'
import { fetchWechatQrCode, pollWechatStatus } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const qrCodeUrl = ref('')
const state = ref('')
const status = ref<'PENDING' | 'SCANNED' | 'CONFIRMED' | 'EXPIRED'>('PENDING')
let pollTimer: ReturnType<typeof setInterval> | null = null

const hintText = computed(() => {
  switch (status.value) {
    case 'PENDING':
      return '请使用微信扫一扫登录'
    case 'SCANNED':
      return '扫码成功，请在手机端确认'
    case 'CONFIRMED':
      return '登录成功'
    case 'EXPIRED':
      return '二维码已过期'
    default:
      return '请使用微信扫一扫登录'
  }
})

async function loadQrCode() {
  try {
    const res = await fetchWechatQrCode()
    qrCodeUrl.value = res.qrCodeUrl
    state.value = res.state
    status.value = 'PENDING'
    startPolling()
  } catch (e) {
    ElMessage.error('二维码获取失败，请稍后再试')
  }
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(async () => {
    if (!state.value) return
    try {
      const res = await pollWechatStatus(state.value)
      status.value = res.status
      if (res.status === 'CONFIRMED' && res.loginResult) {
        stopPolling()
        authStore.setSession(res.loginResult)
        ElMessage.success('微信登录成功')
        const redirect = (route.query.redirect as string) || '/'
        setTimeout(() => router.replace(redirect), 500)
      } else if (res.status === 'EXPIRED') {
        stopPolling()
      }
    } catch {
      // 静默忽略一次性网络抖动，等待下次轮询
    }
  }, 2000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function refresh() {
  qrCodeUrl.value = ''
  loadQrCode()
}

onMounted(loadQrCode)
onBeforeUnmount(stopPolling)
</script>

<style scoped>
.wechat-login {
  text-align: center;
  padding: 1rem 0;
}
.qr-box {
  position: relative;
  width: 220px;
  height: 220px;
  margin: 0 auto 1rem;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  overflow: hidden;
}
.qr-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.qr-placeholder {
  color: #67c23a;
  text-align: center;
}
.qr-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
.qr-mask {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #606266;
  font-size: 14px;
}
.qr-mask.success {
  color: #67c23a;
}
.hint {
  color: #606266;
  font-size: 14px;
  margin: 0.5rem 0 0.25rem;
}
.sub-hint {
  color: #c0c4cc;
  font-size: 12px;
  margin: 0;
}
</style>
