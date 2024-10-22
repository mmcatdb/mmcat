<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import { useFixedRouter } from '@/router/specificRoutes';

const categoryId = useSchemaCategoryId();

const datasources = shallowRef<{
    inCategory: Datasource[];
    other: Datasource[];
}>();

async function fetchDatasources() {
    const results = await Promise.all([
        API.datasources.getAllDatasources({}),
        API.datasources.getAllDatasources({}, { categoryId }),
    ]);
    if (!results[0].status || !results[1].status)
        return false;

    const inCategory = results[1].data.map(Datasource.fromServer);
    const other = results[0].data
        .filter(datasource => !inCategory.find(d => d.id === datasource.id))
        .map(Datasource.fromServer);

    datasources.value = { inCategory, other };
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
        <h1>Datasources in category</h1>
        <template v-if="datasources && 'inCategory' in datasources">
            <div class="d-flex flex-wrap mt-3 gap-3 align-items-stretch">
                <DatasourceDisplay
                    v-for="datasource in datasources.inCategory"
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
            <h1 class="mt-3">
                Other datasources
            </h1>
            <div class="d-flex flex-wrap mt-3 gap-3 align-items-stretch">
                <DatasourceDisplay
                    v-for="datasource in datasources.other"
                    :key="datasource.id"
                    :datasource="datasource"
                    @edit="edit(datasource.id)"
                />
            </div>
        </template>
        <ResourceLoader :loading-function="fetchDatasources" />
    </div>
</template>
