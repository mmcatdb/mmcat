<script setup lang="ts">
import { RouterLink } from 'vue-router';
import TopBar from '@/components/layout/TopBar.vue';
import LeftBar from '@/components/layout/LeftBar.vue';
import Content from '@/components/layout/Content.vue';
import BackToAtlasLink from '@/components/integration/BackToAtlasLink.vue';
</script>

<template>
    <TopBar>
        <template #left>
            <RouterLink
                :to="{ name: 'home' }"
                class="home-link"
            >
                MM-evocat
            </RouterLink>
        </template>
        <template #center>
            <BackToAtlasLink />
        </template>
    </TopBar>
    <LeftBar />
    <Content />
</template>

<style>
#app {
    font-weight: normal;
}

.home-link {
    padding: 0 16px;
    font-size: 20px;
    color: var(--color-background) !important;
    font-weight: 600;
}
</style>
