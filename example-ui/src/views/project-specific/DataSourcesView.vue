<script setup lang="ts">
import { onMounted, ref } from 'vue';
import API from '@/utils/api';
import type { DataSource } from '@/types/dataSource';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import DataSourceDisplay from '@/components/dataSource/DataSourceDisplay.vue';
import { useRouter } from 'vue-router';
import { tryUseSchemaCategory } from '@/utils/globalSchemaSettings';

const dataSources = ref<DataSource[]>();
const fetched = ref(false);

onMounted(() => {
    fetchData();
});

async function fetchData() {
    const categoryId = tryUseSchemaCategory();
    const queryParams = categoryId !== undefined ? { categoryId } : undefined;
    const result = await API.dataSources.getAllDataSources({}, queryParams);
    if (result.status)
        dataSources.value = result.data;

    fetched.value = true;
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
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.data-sources {
    display: flex;
    flex-wrap: wrap;
}
</style>
