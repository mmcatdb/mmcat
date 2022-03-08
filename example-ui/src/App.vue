<script setup lang="ts">
import { RouterLink } from 'vue-router';
import TopBar from './components/layout/TopBar.vue';
import LeftBar from './components/layout/LeftBar.vue';
import Content from './components/layout/Content.vue';
</script>

<template>
    <TopBar>

    </TopBar>
    <LeftBar>
        <RouterLink :to="{ name: 'home' }">
            Home
        </RouterLink>
        <RouterLink :to="{ name: 'jobs' }">
            Jobs
        </RouterLink>
        <RouterLink :to="{ name: 'newJob' }">
            New job
        </RouterLink>
        <RouterLink :to="{ name: 'schema' }">
            Schema category
        </RouterLink>
        <RouterLink :to="{ name: 'test' }">
            Test
        </RouterLink>
    </LeftBar>
    <Content />
</template>

<style>
@import "@/assets/base.css";

#app {
    font-weight: normal;
}
</style>
