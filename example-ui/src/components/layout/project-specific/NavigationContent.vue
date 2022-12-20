<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router';

type Link = {
    pathName: string,
    label: string,
    params: {
        schemaCategoryId: string | string[]
    }
};

const route = useRoute();

function defineLink(pathName: string, label: string): Link {
    return { pathName, label, params: {
        schemaCategoryId: route.params.schemaCategoryId
    } };
}

const links = [
    defineLink('schemaCategory', 'Schema Category'),
    defineLink('logicalModels', 'Logical Models'),
    defineLink('databases', 'Databases'),
    defineLink('jobs', 'Jobs'),
    defineLink('instanceCategory', 'Instance Category'),
    defineLink('models', 'Models')
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
