<script setup lang="ts">
import API from '@/utils/api';
import type { DataSource } from '@/types/dataSource';

import ResourceLoader from '@/components/ResourceLoader.vue';
import DataSourceDisplay from '@/components/dataSource/DataSourceDisplay.vue';
import DataSourceEditor from '@/components/dataSource/DataSourceEditor.vue';
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toQueryScalar } from '@/utils/router';

const route = useRoute();
const router = useRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? null : parseInt(rawId);

const isEditing = ref(isNew || route.query.state === 'editing');
const categoryId = toQueryScalar(route.query.categoryId);
const returnPath = categoryId
    ? { name: 'dataSourcesInCategory', params: { categoryId } }
    : { name: 'dataSources' };

const dataSource = ref<DataSource>();

const shouldReturnToAllDataSourcesAfterEditing = isEditing.value;

async function fetchDataSource() {
    if (isNew || !id)
        return true;

    const result = await API.dataSources.getDataSource({ id: id });
    if (!result.status)
        return false;

    dataSource.value = result.data;
    return true;
}

function save(newValue: DataSource) {
    if (shouldReturnToAllDataSourcesAfterEditing) {
        router.push(returnPath);
        return;
    }

    dataSource.value = newValue;
    isEditing.value = false;
}

function cancel() {
    if (shouldReturnToAllDataSourcesAfterEditing) {
        router.push(returnPath);
        return;
    }

    isEditing.value = false;
}
</script>

<template>
    <div>
        <template v-if="isNew">
            <h1>Create new data source</h1>
            <div class="data-source">
                <DataSourceEditor
                    @save="save"
                    @cancel="cancel"
                />
            </div>
        </template>
        <template v-else>
            <h1>Data source</h1>
            <div
                v-if="dataSource"
                class="data-source"
            >
                <DataSourceEditor
                    v-if="isEditing"
                    :data-source="dataSource"
                    @save="save"
                    @cancel="cancel"
                    @delete="router.push(returnPath)"
                />
                <DataSourceDisplay
                    v-else
                    :data-source="dataSource"
                    @edit="isEditing = true"
                />
            </div>
            <ResourceLoader :loading-function="fetchDataSource" />
            <div class="button-row">
                <button
                    v-if="!isEditing"
                    @click="router.push(returnPath)"
                >
                    Back
                </button>
            </div>
        </template>
    </div>
</template>

<style scoped>
.data-source {
    display: flex;
}
</style>
