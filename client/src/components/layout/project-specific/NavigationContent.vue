<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router';

type Link = {
    pathName: string;
    label: string;
    params: {
        categoryId: string | string[];
    };
};

const route = useRoute();

function defineLink(pathName: string, label: string): Link {
    return { pathName, label, params: {
        categoryId: route.params.categoryId,
    } };
}

const links = [
    defineLink('schemaCategory', 'Schema Category'),
    defineLink('logicalModels', 'Logical Models'),
    defineLink('databasesInCategory', 'Databases'),
    defineLink('dataSourcesInCategory', 'Data sources'),
    defineLink('jobs', 'Jobs'),
    defineLink('instanceCategory', 'Instance Category'),
    defineLink('models', 'Models'),
    defineLink('query', 'Querying'),
    defineLink('savedQueries', 'Saved Queries'),
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
