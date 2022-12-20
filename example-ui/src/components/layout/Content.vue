<script setup lang="ts">
import { RouterView } from 'vue-router';
</script>

<template>
    <main class="outer-content">
        <div class="inner-content">
            <RouterView />
        </div>
    </main>
</template>

<style scoped>
.outer-content {
    padding-top: var(--top-bar-height);
    padding-left: var(--left-bar-width);
}

.inner-content {
    padding: 10px 20px;
}
</style>
