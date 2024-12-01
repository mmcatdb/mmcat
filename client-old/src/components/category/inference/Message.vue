<script setup lang="ts">
const props = defineProps<{
  show: boolean;
  message: string;
}>();
</script>

<template>
    <div
        v-if="show"
        class="message"
    >
        <p>{{ message }}</p>
    </div>
</template>
  
<style scoped>
.message {
    position: absolute;
    top: 1px;
    left: 50%;
    transform: translateX(-50%);
    background-color: rgba(90, 213, 28, 0.8);
    color: white;
    padding: 20px;
    border-radius: 10px;
    font-size: 1.2rem;
    font-weight: bold;
    text-align: center;
    width: 90%;
    z-index: 1000;
}
</style>

  