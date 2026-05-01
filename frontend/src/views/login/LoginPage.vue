<template>
  <div class="login-page">
    <!-- 左侧轮播图 -->
    <div class="left">
      <CarouselSection />
    </div>

    <!-- 右侧登录区 -->
    <div class="right">
      <div class="login-box">
        <div class="brand">
          <h1>AI Demo</h1>
          <p>欢迎回来，请登录您的账户</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs" stretch>
          <el-tab-pane label="账号密码登录" name="password">
            <PasswordLoginForm />
          </el-tab-pane>
          <el-tab-pane label="手机号登录" name="phone">
            <PhoneLoginForm />
          </el-tab-pane>
          <el-tab-pane label="微信扫码" name="wechat">
            <WechatLoginForm />
          </el-tab-pane>
        </el-tabs>

        <p class="footer">还没账号？<el-link type="primary" :underline="false">立即注册</el-link></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CarouselSection from './components/CarouselSection.vue'
import PasswordLoginForm from './components/PasswordLoginForm.vue'
import PhoneLoginForm from './components/PhoneLoginForm.vue'
import WechatLoginForm from './components/WechatLoginForm.vue'

const activeTab = ref<'password' | 'phone' | 'wechat'>('password')
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  width: 100%;
}
.left {
  flex: 1;
  overflow: hidden;
}
.right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 2rem;
}
.login-box {
  width: 100%;
  max-width: 400px;
}
.brand {
  text-align: center;
  margin-bottom: 2rem;
}
.brand h1 {
  margin: 0 0 0.5rem;
  font-size: 2rem;
  color: #303133;
}
.brand p {
  margin: 0;
  color: #909399;
}
.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
}
.footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin-top: 1.5rem;
}

/* 响应式：小屏堆叠 */
@media (max-width: 768px) {
  .login-page {
    flex-direction: column;
  }
  .left {
    flex: none;
    height: 260px;
  }
  .right {
    padding: 1.5rem;
  }
}
</style>
