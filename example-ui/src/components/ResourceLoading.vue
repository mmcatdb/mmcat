<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

const LOADING_WAIT_TIME_IN_MILLISECONDS = 400;

const showLoadingMessage = ref(false);

const timeoutId = ref<number>();
onMounted(() => {
    timeoutId.value = setTimeout(() => {
        showLoadingMessage.value = true;
    }, LOADING_WAIT_TIME_IN_MILLISECONDS);
});

onUnmounted(() => clearTimeout(timeoutId.value));
</script>

<template>
    <p v-if="showLoadingMessage">
        Loading ....
    </p>
</template>
