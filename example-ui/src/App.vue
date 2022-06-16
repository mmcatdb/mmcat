<script setup lang="ts">
import { RouterLink } from 'vue-router';
import TopBar from './components/layout/TopBar.vue';
import LeftBar from './components/layout/LeftBar.vue';
import Content from './components/layout/Content.vue';

type Link = {
    pathName: string,
    label: string
}

function defineLink(pathName: string, label: string): Link {
    return { pathName, label };
}

const links = [
    defineLink('home', 'Home'),
    defineLink('jobs', 'Jobs'),
    defineLink('accessPathEditor', 'Create Mapping'),
    defineLink('schema', 'Schema Category'),
    defineLink('instances', 'Instances'),
    defineLink('instanceCategory', 'Instance Category'),
    defineLink('databases', 'Databases'),
];
</script>

<template>
    <TopBar />
    <LeftBar>
        <RouterLink
            v-for="link in links"
            :key="link.pathName"
            :to="{ name: link.pathName }"
        >
            {{ link.label }}
        </RouterLink>
    </LeftBar>
    <Content />
</template>

<style>
@import "@/assets/base.css";
@import "@/assets/componentLayout.css";
@import "@/assets/buttons.css";

#app {
    font-weight: normal;
}
</style>
