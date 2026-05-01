<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    @keyup.enter="handleSubmit"
  >
    <el-form-item label="手机号" prop="phone">
      <el-input v-model="form.phone" placeholder="请输入手机号" size="large" clearable maxlength="11">
        <template #prefix>
          <el-icon><Iphone /></el-icon>
        </template>
      </el-input>
    </el-form-item>

    <el-form-item label="验证码" prop="code">
      <div class="code-row">
        <el-input v-model="form.code" placeholder="请输入验证码" size="large" maxlength="6">
          <template #prefix>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
        <el-button size="large" :disabled="countdown > 0" @click="handleSendCode">
          {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
        </el-button>
      </div>
    </el-form-item>

    <el-button
      type="primary"
      size="large"
      :loading="loading"
      class="submit-btn"
      @click="handleSubmit"
    >
      登 录
    </el-button>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { sendSmsCode } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  phone: '',
  code: ''
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度不正确', trigger: 'blur' }
  ]
}

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && timer) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

async function handleSendCode() {
  if (!formRef.value) return
  const valid = await formRef.value.validateField('phone').catch(() => false)
  if (!valid) return
  try {
    await sendSmsCode(form.phone)
    ElMessage.success('验证码已发送（测试环境固定为 123456）')
    startCountdown()
  } catch {
    /* ignore */
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.loginByPhone(form.phone, form.code)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.code-row .el-input {
  flex: 1;
}
.submit-btn {
  width: 100%;
}
</style>
