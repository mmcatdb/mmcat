<script setup lang="ts">
import IndependentNavigation from '@/components/layout/navigation/IndependentNavigation.vue';
import { RouterView } from 'vue-router';

</script>

<template>
    <RouterView />
    <Teleport to="#app-left-bar-content">
        <IndependentNavigation />
    </Teleport>
</template>
