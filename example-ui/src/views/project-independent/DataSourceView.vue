<script setup lang="ts">
import API from '@/utils/api';
import type { DataSource } from '@/types/dataSource';

import ResourceLoader from '@/components/ResourceLoader.vue';
import DataSourceDisplay from '@/components/dataSource/DataSourceDisplay.vue';
import DataSourceEditor from '@/components/dataSource/DataSourceEditor.vue';
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? null : parseInt(rawId);

const isEditing = ref(isNew || route.params.state === 'editing');
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
        router.push({ name: 'dataSources' });
        return;
    }

    dataSource.value = newValue;
    isEditing.value = false;
}

function cancel() {
    if (shouldReturnToAllDataSourcesAfterEditing) {
        router.push({ name: 'dataSources' });
        return;
    }

    isEditing.value = false;
}

function deleteFunction() {
    router.push({ name: 'dataSources' });
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
                    @delete="deleteFunction"
                />
                <DataSourceDisplay
                    v-else
                    :data-source="dataSource"
                    @edit="isEditing = true"
                />
            </div>
            <ResourceLoader :loading-function="fetchDataSource" />
        </template>
    </div>
</template>

<style scoped>
.data-source {
    display: flex;
}
</style>
