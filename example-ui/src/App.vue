<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import TopBar from '@/components/layout/TopBar.vue';
import LeftBar from '@/components/layout/LeftBar.vue';
import Content from '@/components/layout/Content.vue';

type Link = {
    pathName: string,
    label: string
}

function defineLink(pathName: string, label: string): Link {
    return { pathName, label };
}

const globalLinks = [
    defineLink('home', 'MM-evocat'),
];
</script>

<template>
    <TopBar />
    <LeftBar>
        <RouterLink
            v-for="link in globalLinks"
            :key="link.pathName"
            :to="{ name: link.pathName }"
        >
            {{ link.label }}
        </RouterLink>
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
</style>
