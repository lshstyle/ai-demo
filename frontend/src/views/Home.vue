<template>
  <div class="home">
    <h2>首页</h2>
    <p>欢迎来到 AI Demo 项目！</p>
    <HelloWorld />
    <div class="counter-section">
      <p>计数器: {{ counter.count }}</p>
      <p>双倍: {{ counter.doubleCount }}</p>
      <button @click="counter.increment">增加</button>
    </div>
    <div class="api-section">
      <h3>后端接口测试</h3>
      <button @click="fetchMessage">获取后端消息</button>
      <p v-if="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useCounterStore } from '@/stores/counter'
import HelloWorld from '@/components/HelloWorld.vue'
import axios from 'axios'

const counter = useCounterStore()
const message = ref('')

const fetchMessage = async () => {
  try {
    const res = await axios.get('/api/hello')
    message.value = res.data
  } catch (e) {
    message.value = '请求失败，请确保后端服务已启动'
  }
}
</script>

<style scoped>
.home {
  max-width: 800px;
  margin: 0 auto;
}

.counter-section, .api-section {
  margin-top: 2rem;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
}

button {
  padding: 0.5rem 1rem;
  background: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background: #369970;
}
</style>
