<script setup lang="ts">
import TopBar from '@/components/layout/TopBar.vue';
import LeftBar from '@/components/layout/LeftBar.vue';
import Content from '@/components/layout/Content.vue';
import FixedRouterLink from './components/common/FixedRouterLink.vue';
</script>

<template>
    <TopBar>
        <template #left>
            <FixedRouterLink
                :to="{ name: 'home' }"
                class="home-link"
                always-link
            >
                MM-cat
            </FixedRouterLink>
        </template>
        <template #center />
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
