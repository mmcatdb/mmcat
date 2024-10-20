<script setup lang="ts">
import API from '@/utils/api';
import type { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import DatasourceEditor from '@/components/datasource/DatasourceEditor.vue';
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import { useFixedRouter } from '@/router/specificRoutes';

const route = useRoute();
const router = useFixedRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? undefined : rawId;

const isEditing = ref(isNew || route.query.state === 'editing');

const datasource = ref<Datasource>();

const shouldReturnToAllDatasourcesAfterEditing = isEditing.value;

async function fetchDatasource() {
    if (isNew || !id)
        return true;

    const result = await API.datasources.getDatasource({ id: id });
    if (!result.status)
        return false;

    datasource.value = result.data;
    return true;
}

function save(newValue: Datasource) {
    if (shouldReturnToAllDatasourcesAfterEditing) {
        router.push({ name: 'datasources' });
        return;
    }

    datasource.value = newValue;
    isEditing.value = false;
}

function cancel() {
    if (shouldReturnToAllDatasourcesAfterEditing) {
        router.push({ name: 'datasources' });
        return;
    }

    isEditing.value = false;
}
</script>

<template>
    <div>
        <template v-if="isNew">
            <h1>Create new datasource</h1>
            <div class="d-flex mt-3">
                <DatasourceEditor
                    @save="save"
                    @cancel="cancel"
                />
            </div>
        </template>
        <template v-else>
            <h1>Datasource</h1>
            <div
                v-if="datasource"
                class="d-flex mt-3"
            >
                <DatasourceEditor
                    v-if="isEditing"
                    :datasource="datasource"
                    @save="save"
                    @cancel="cancel"
                    @delete="router.push({ name: 'datasources' })"
                />
                <DatasourceDisplay
                    v-else
                    :datasource="datasource"
                    @edit="isEditing = true"
                />
            </div>
            <ResourceLoader :loading-function="fetchDatasource" />
            <div class="button-row">
                <button
                    v-if="!isEditing"
                    @click="router.push({ name: 'datasources' })"
                >
                    Back
                </button>
            </div>
        </template>
    </div>
</template>
