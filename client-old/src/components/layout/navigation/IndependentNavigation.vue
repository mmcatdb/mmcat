<script setup lang="ts">
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';

const links = [
    { name: 'home', label: 'Home' },
    { name: 'datasources', label: 'Datasources' },
    { name: 'about', label: 'About' },
];
</script>

<template>
    <FixedRouterLink
        v-for="link in links"
        :key="link.name"
        :to="{ name: link.name }"
        always-link
    >
        {{ link.label }}
    </FixedRouterLink>
</template>
