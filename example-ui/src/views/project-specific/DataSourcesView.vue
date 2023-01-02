<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { DataSource } from '@/types/dataSource';

import ResourceLoader from '@/components/ResourceLoader.vue';
import DataSourceDisplay from '@/components/dataSource/DataSourceDisplay.vue';
import { useRouter } from 'vue-router';
import { tryUseSchemaCategory } from '@/utils/globalSchemaSettings';

const dataSources = ref<DataSource[]>();

async function fetchDataSources() {
    const categoryId = tryUseSchemaCategory();
    const queryParams = categoryId !== undefined ? { categoryId } : undefined;
    const result = await API.dataSources.getAllDataSources({}, queryParams);
    if (!result.status)
        return false;

    dataSources.value = result.data;
    return true;
}

const router = useRouter();

function createNew() {
    router.push({ name: 'dataSource', params: { id: 'new' } });
}
</script>

<template>
    <div>
        <h1>Data Sources</h1>
        <template v-if="dataSources">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
            <div
                class="data-sources"
            >
                <div
                    v-for="dataSource in dataSources"
                    :key="dataSource.id"
                >
                    <DataSourceDisplay
                        :data-source="dataSource"
                        @edit="$router.push({ name: 'dataSource', params: { id: dataSource.id, state: 'editing' } });"
                    />
                </div>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchDataSources" />
    </div>
</template>

<style scoped>
.data-sources {
    display: flex;
    flex-wrap: wrap;
}
</style>
