<template>
  <div id="app">
    <template v-if="!hideLayout">
      <header>
        <h1>AI Demo</h1>
        <nav>
          <router-link to="/">首页</router-link>
          <router-link to="/about">关于</router-link>
          <el-link type="danger" :underline="false" class="logout" @click="handleLogout">
            退出登录
          </el-link>
        </nav>
      </header>
      <main>
        <router-view />
      </main>
    </template>
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const hideLayout = computed(() => route.meta?.hideLayout === true)

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
header {
  background: #42b883;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}
nav a {
  color: white;
  text-decoration: none;
}
nav a.router-link-active {
  font-weight: bold;
  text-decoration: underline;
}
.logout {
  margin-left: 0.5rem;
  color: #fff !important;
}
main {
  padding: 2rem;
}
</style>
