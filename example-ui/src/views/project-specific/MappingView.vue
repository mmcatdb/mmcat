<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';

import { Mapping } from '@/types/mapping';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import ResourceLoader from '@/components/ResourceLoader.vue';
import { useRoute } from 'vue-router';

const mapping = ref<Mapping>();

const route = useRoute();

async function fetchMapping() {
    const result = await API.mappings.getMapping({ id: route.params.id });
    if (!result.status)
        return false;

    mapping.value = Mapping.fromServer(result.data);
    return true;
}
</script>

<template>
    <h1>Mapping</h1>
    <div
        v-if="mapping"
        class="mapping"
    >
        <MappingDisplay
            :mapping="mapping"
        />
    </div>
    <ResourceLoader :loading-function="fetchMapping" />
</template>

<style scoped>
.mapping {
    display: flex;
}
</style>
