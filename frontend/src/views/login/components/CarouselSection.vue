<template>
  <div class="carousel-section">
    <el-carousel :interval="4000" indicator-position="outside" height="100vh">
      <el-carousel-item v-for="(item, idx) in slides" :key="idx">
        <div class="slide" :style="{ background: item.bg }">
          <div class="slide-text">
            <h2>{{ item.title }}</h2>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup lang="ts">
interface Slide {
  title: string
  desc: string
  bg: string
}

const slides: Slide[] = [
  {
    title: 'Vue 3 + Spring Cloud',
    desc: '一站式全栈开发示例',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '微服务架构',
    desc: 'Gateway + User Service 灵活扩展',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '多种登录方式',
    desc: '账号密码 / 微信扫码 / 手机验证码',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
]
</script>

<style scoped>
.carousel-section {
  width: 100%;
  height: 100vh;
}
.slide {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.slide-text {
  text-align: center;
  padding: 0 2rem;
}
.slide-text h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}
.slide-text p {
  font-size: 1.2rem;
  opacity: 0.9;
}
</style>
