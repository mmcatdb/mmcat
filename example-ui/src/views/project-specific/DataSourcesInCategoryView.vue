<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { DataSource } from '@/types/dataSource';

import ResourceLoader from '@/components/ResourceLoader.vue';
import DataSourceDisplay from '@/components/dataSource/DataSourceDisplay.vue';
import { useRouter } from 'vue-router';
import { useSchemaCategoryId } from '@/utils/globalSchemaSettings';
import type { Id } from '@/types/id';

const dataSourcesInCategory = ref<DataSource[]>();
const dataSourcesOther = ref<DataSource[]>();
const categoryId = useSchemaCategoryId();

async function fetchDataSources() {
    const [ resultInCategory, resultOther ] = await Promise.all([
        API.dataSources.getAllDataSources({}, { categoryId }),
        API.dataSources.getAllDataSources({})
    ]);

    if (!resultInCategory.status || !resultOther.status)
        return false;

    dataSourcesInCategory.value = resultInCategory.data;
    dataSourcesOther.value = resultOther.data.filter(dataSource => !resultInCategory.data.find(d => d.id === dataSource.id));

    return true;
}

const router = useRouter();

function createNew() {
    router.push({ name: 'dataSource', params: { id: 'new' }, query: { categoryId } });
}

function edit(id: Id) {
    router.push({ name: 'dataSource', params: { id }, query: { categoryId, state: 'editing' } });
}
</script>

<template>
    <div>
        <h1>Data Sources in category</h1>
        <div class="data-sources mt-3">
            <div
                v-for="dataSource in dataSourcesInCategory"
                :key="dataSource.id"
            >
                <DataSourceDisplay
                    :data-source="dataSource"
                    :category-id="categoryId"
                    @edit="edit(dataSource.id)"
                />
            </div>
        </div>
        <template v-if="dataSourcesInCategory">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
        </template>
        <h1>Other data sources</h1>
        <div class="data-sources mt-3">
            <div
                v-for="dataSource in dataSourcesOther"
                :key="dataSource.id"
            >
                <DataSourceDisplay
                    :data-source="dataSource"
                    :category-id="categoryId"
                    @edit="edit(dataSource.id)"
                />
            </div>
        </div>
        <ResourceLoader :loading-function="fetchDataSources" />
    </div>
</template>

<style scoped>
.data-sources {
    display: flex;
    flex-wrap: wrap;
}
</style>
