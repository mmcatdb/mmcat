<script setup lang="ts">
import { RouterLink } from 'vue-router';

type Link = {
    pathName: string;
    label: string;
};

function defineLink(pathName: string, label: string): Link {
    return { pathName, label };
}

const links = [
    defineLink('home', 'Home'),
    defineLink('datasources', 'Datasources'),
    defineLink('about', 'About'),
];
</script>

<template>
    <RouterLink
        v-for="link in links"
        :key="link.pathName"
        :to="{ name: link.pathName }"
    >
        {{ link.label }}
    </RouterLink>
</template>
