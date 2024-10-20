<script setup lang="ts">
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';

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
    <FixedRouterLink
        v-for="link in links"
        :key="link.pathName"
        :to="{ name: link.pathName }"
        always-link
    >
        {{ link.label }}
    </FixedRouterLink>
</template>
