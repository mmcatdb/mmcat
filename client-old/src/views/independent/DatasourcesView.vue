<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import type { Id } from '@/types/id';
import { useFixedRouter } from '@/router/specificRoutes';

const datasources = shallowRef<{
    all: Datasource[];
}>();

async function fetchDatasources() {
    const result = await API.datasources.getAllDatasources({});
    if (!result.status)
        return false;

    const all = result.data.map(Datasource.fromServer);

    datasources.value = { all };
    return true;
}

const router = useFixedRouter();

function createNew() {
    router.push({ name: 'datasource', params: { id: 'new' } });
}

function edit(id: Id) {
    router.push({ name: 'datasource', params: { id }, query: { state: 'editing' } });
}
</script>

<template>
    <div>
        <h1>Datasources</h1>
        <template v-if="datasources && 'all' in datasources">
            <div class="d-flex flex-wrap mt-3 gap-3 align-items-stretch">
                <DatasourceDisplay
                    v-for="datasource in datasources.all"
                    :key="datasource.id"
                    :datasource="datasource"
                    @edit="edit(datasource.id)"
                />
                <button
                    class="fs-3 p-5"
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchDatasources" />
    </div>
</template>
