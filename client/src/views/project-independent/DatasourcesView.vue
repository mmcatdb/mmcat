<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import { useRouter } from 'vue-router';

const datasources = ref<Datasource[]>();

async function fetchDatasources() {
    const result = await API.datasources.getAllDatasources({});
    if (!result.status)
        return false;

    datasources.value = result.data;
    return true;
}

const router = useRouter();

function createNew() {
    router.push({ name: 'datasource', params: { id: 'new' } });
}
</script>

<template>
    <div>
        <h1>Data Sources</h1>
        <div class="d-flex flex-wrap mt-3">
            <div
                v-for="datasource in datasources"
                :key="datasource.id"
            >
                <DatasourceDisplay
                    :datasource="datasource"
                    @edit="$router.push({ name: 'datasource', params: { id: datasource.id }, query: { state: 'editing' } });"
                />
            </div>
        </div>
        <template v-if="datasources">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchDatasources" />
    </div>
</template>
