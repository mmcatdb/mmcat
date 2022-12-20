<script setup lang="ts">
import NavigationContent from '@/components/layout/project-independent/NavigationContent.vue';
import { RouterView } from 'vue-router';

</script>

<template>
    <RouterView />
    <Teleport to="#app-left-bar-content">
        <NavigationContent />
    </Teleport>
</template>
