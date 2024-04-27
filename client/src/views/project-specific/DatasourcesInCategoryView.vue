<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import { useRouter } from 'vue-router';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';

const datasourcesInCategory = ref<Datasource[]>();
const datasourcesOther = ref<Datasource[]>();
const categoryId = useSchemaCategoryId();

async function fetchDatasources() {
    const [ resultInCategory, resultOther ] = await Promise.all([
        API.datasources.getAllDatasources({}, { categoryId }),
        API.datasources.getAllDatasources({}),
    ]);

    if (!resultInCategory.status || !resultOther.status)
        return false;

    datasourcesInCategory.value = resultInCategory.data.map(Datasource.fromServer);
    datasourcesOther.value = resultOther.data
        .filter(datasource => !resultInCategory.data.find(d => d.id === datasource.id))
        .map(Datasource.fromServer);

    return true;
}

const router = useRouter();

function createNew() {
    router.push({ name: 'datasource', params: { id: 'new' }, query: { categoryId } });
}

function edit(id: Id) {
    router.push({ name: 'datasource', params: { id }, query: { categoryId, state: 'editing' } });
}
</script>

<template>
    <div>
        <h1>Data Sources in category</h1>
        <div class="d-flex flex-wrap mt-3">
            <div
                v-for="datasource in datasourcesInCategory"
                :key="datasource.id"
            >
                <DatasourceDisplay
                    :datasource="datasource"
                    :category-id="categoryId"
                    @edit="edit(datasource.id)"
                />
            </div>
        </div>
        <template v-if="datasourcesInCategory">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
        </template>
        <h1>Other data sources</h1>
        <div class="d-flex flex-wrap mt-3">
            <div
                v-for="datasource in datasourcesOther"
                :key="datasource.id"
            >
                <DatasourceDisplay
                    :datasource="datasource"
                    :category-id="categoryId"
                    @edit="edit(datasource.id)"
                />
            </div>
        </div>
        <ResourceLoader :loading-function="fetchDatasources" />
    </div>
</template>
