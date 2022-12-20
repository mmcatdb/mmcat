<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import TopBar from '@/components/layout/TopBar.vue';
import LeftBar from '@/components/layout/LeftBar.vue';
import Content from '@/components/layout/Content.vue';
</script>

<template>
    <TopBar class="app-top-bar">
        <div>
            <RouterLink
                :to="{ name: 'home' }"
                class="home-link"
            >
                MM-evocat
            </RouterLink>
        </div>
    </TopBar>
    <LeftBar>
        <RouterView name="leftBar" />
    </LeftBar>
    <Content />
</template>

<style>
@import "@/assets/base.css";
@import "@/assets/componentLayout.css";
@import "@/assets/buttons.css";
@import "@/assets/links.css";
/*/ @import "@/assets/screenshot.css"; /**/

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
