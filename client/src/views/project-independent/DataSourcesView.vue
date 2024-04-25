<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { DataSource } from '@/types/dataSource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DataSourceDisplay from '@/components/dataSource/DataSourceDisplay.vue';
import { useRouter } from 'vue-router';

const dataSources = ref<DataSource[]>();

async function fetchDataSources() {
    const result = await API.dataSources.getAllDataSources({});
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
        <div class="d-flex flex-wrap mt-3">
            <div
                v-for="dataSource in dataSources"
                :key="dataSource.id"
            >
                <DataSourceDisplay
                    :data-source="dataSource"
                    @edit="$router.push({ name: 'dataSource', params: { id: dataSource.id }, query: { state: 'editing' } });"
                />
            </div>
        </div>
        <template v-if="dataSources">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchDataSources" />
    </div>
</template>
