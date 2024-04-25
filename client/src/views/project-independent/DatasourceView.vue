<script setup lang="ts">
import API from '@/utils/api';
import type { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import DatasourceEditor from '@/components/datasource/DatasourceEditor.vue';
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
    ? { name: 'datasourcesInCategory', params: { categoryId } }
    : { name: 'datasources' };

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
        router.push(returnPath);
        return;
    }

    datasource.value = newValue;
    isEditing.value = false;
}

function cancel() {
    if (shouldReturnToAllDatasourcesAfterEditing) {
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
            <div class="d-flex mt-3">
                <DatasourceEditor
                    @save="save"
                    @cancel="cancel"
                />
            </div>
        </template>
        <template v-else>
            <h1>Data Source</h1>
            <div
                v-if="datasource"
                class="d-flex mt-3"
            >
                <DatasourceEditor
                    v-if="isEditing"
                    :datasource="datasource"
                    @save="save"
                    @cancel="cancel"
                    @delete="router.push(returnPath)"
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
                    @click="router.push(returnPath)"
                >
                    Back
                </button>
            </div>
        </template>
    </div>
</template>
