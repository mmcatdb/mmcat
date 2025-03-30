<script setup lang="ts">
import API from '@/utils/api';
import { isFile, type Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import DatasourceEditor from '@/components/datasource/DatasourceEditor.vue';
import { computed, shallowRef } from 'vue';
import { useRoute } from 'vue-router';
import { useFixedRouter } from '@/router/specificRoutes';
import { Mapping } from '@/types/mapping';
import { tryUseSchemaCategoryId, tryUseWorkflow } from '@/utils/injects';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';

const route = useRoute();
const router = useFixedRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? undefined : rawId;

const isEditing = shallowRef(isNew || route.query.state === 'editing');

const datasource = shallowRef<Datasource>();

const shouldReturnToAllDatasourcesAfterEditing = isEditing.value;

const categoryId = tryUseSchemaCategoryId();
const mappings = shallowRef<Mapping[]>();

async function fetchDatasource() {
    if (isNew || !id)
        return true;

    const result = await API.datasources.getDatasource({ id: id });
    if (!result.status)
        return false;

    datasource.value = result.data;

    if (!categoryId)
        return true;

    const mappingsResult = await API.mappings.getAllMappingsInCategory({}, { categoryId, datasourceId: id });
    if (!mappingsResult.status)
        return false;

    mappings.value = mappingsResult.data.map(Mapping.fromServer);

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

function createMapping() {
    router.push({ name: 'accessPathEditor', query: { datasourceId: route.params.id } });
}

const isForFile = computed(() => datasource.value?.type && isFile(datasource.value.type));

const workflow = tryUseWorkflow()?.value;

function isMappingsVisible() {
    if (isEditing.value)
        return false;
    if (!workflow)
        return true;
    
    return [ 'addMappings', 'finish' ].includes(workflow.data.step);
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
                    :scondary-button="'Back'"
                    @edit="isEditing = true"
                    @on-secondary-click="router.push({ name: 'datasources' })"
                />
            </div>
            <template v-if="mappings && isMappingsVisible()">
                <h2 class="mt-3">
                    {{ isForFile ? 'Mapping' : 'Mappings' }}
                </h2>
                <div class="d-flex align-items-center gap-3">
                    <button
                        :disabled="(isForFile && mappings.length > 0)"
                        @click="createMapping"
                    >
                        Create new
                    </button>
                </div>
                <div class="mt-3 d-flex flex-wrap gap-3">
                    <MappingDisplay
                        v-for="mapping in mappings"
                        :key="mapping.id"
                        :mapping="mapping"
                    />
                </div>
            </template>
            <ResourceLoader :loading-function="fetchDatasource" />
        </template>
    </div>
</template>
