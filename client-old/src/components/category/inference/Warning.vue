<script setup lang="ts">
const props = defineProps<{
  show: boolean;
  message: string;
}>();
</script>

<template>
    <div
        v-if="show"
        class="warning"
    >
        <p>{{ message }}</p>
    </div>
</template>
  
  <style scoped>
  .warning {
    position: absolute;
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
    background-color: rgba(255, 0, 0, 0.8);
    color: white;
    padding: 10px;
    border-radius: 5px;
    z-index: 1000;
  }
  </style>
  