<script setup lang="ts">
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';

const links = [
    { name: 'schemaCategory', label: 'Schema Category' },
    { name: 'datasources', label: 'Datasources' },
    { name: 'actions', label: 'Actions' },
    { name: 'jobs', label: 'Jobs' },
    { name: 'instanceCategory', label: 'Instance Category' },
    { name: 'query', label: 'Querying' },
    { name: 'savedQueries', label: 'Saved Queries' },
    { name: 'files', label: 'Outputs' },
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
