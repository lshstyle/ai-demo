<template>
  <div class="wechat-login">
    <div class="qr-box">
      <div class="qr-placeholder">
        <el-icon :size="80"><ChatDotRound /></el-icon>
        <div class="qr-tip">二维码加载中...</div>
      </div>
    </div>
    <p class="hint">请使用微信扫一扫登录</p>
    <p class="sub-hint">（演示环境暂未接入微信开放平台）</p>
  </div>
</template>

<script setup lang="ts">
// 预留：实际接入时请调用 /api/auth/login/wechat
</script>

<style scoped>
.wechat-login {
  text-align: center;
  padding: 1rem 0;
}
.qr-box {
  width: 200px;
  height: 200px;
  margin: 0 auto 1rem;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
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
