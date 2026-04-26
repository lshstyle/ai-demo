<template>
  <div class="hello">
    <h3>{{ msg }}</h3>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  msg?: string
}>()
</script>

<style scoped>
.hello {
  margin: 1rem 0;
}
</style>
