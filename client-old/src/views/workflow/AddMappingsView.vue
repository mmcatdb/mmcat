<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import { useWorkflow } from '@/utils/injects';
import { useFixedRouter } from '@/router/specificRoutes';
import { Mapping } from '@/types/mapping';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import { Datasource } from '@/types/datasource';

const workflow = useWorkflow();
const emit = defineEmits([ 'continue' ]);

const datasources = shallowRef<Datasource[]>();
const mappings = shallowRef<Mapping[]>();

async function fetchData() {
    const datasourcesResult = await API.datasources.getAllDatasources({});
    if (!datasourcesResult.status)
        return false;

    const mappingsResult = await API.mappings.getAllMappingsInCategory({}, { categoryId: workflow.value.categoryId });
    if (!mappingsResult.status)
        return false;

    datasources.value = datasourcesResult.data.map(Datasource.fromServer);
    mappings.value = mappingsResult.data.map(Mapping.fromServer);

    return true;
}

const selectedDatasource = shallowRef<Datasource>();

const router = useFixedRouter();

function createMapping() {
    if (!selectedDatasource.value)
        return;

    router.push({ name: 'accessPathEditor', query: { datasourceId: selectedDatasource.value.id } });
}
</script>

<template>
    <h1>Add mappings</h1>
    <p>
        You should have at least one two mappings ...
    </p>
    <div
        v-if="datasources && mappings"
        class="d-flex flex-wrap gap-3"
    >
        <MappingDisplay
            v-for="mapping in mappings"
            :key="mapping.id"
            :mapping="mapping"
            max-height-path
        />
        <div class="border border-primary p-4 d-flex flex-column align-items-center justify-content-center">
            <label>
                Select datasource:<br />
                <select v-model="selectedDatasource">
                    <option
                        v-for="datasource in datasources"
                        :key="datasource.id"
                        :value="datasource"
                    >
                        {{ datasource.label }}
                    </option>
                </select>
            </label>
            <button
                class="mt-5"
                :disabled="!selectedDatasource"
                @click="createMapping"
            >
                Create new mapping
            </button>
        </div>
    </div>
    <ResourceLoader :loading-function="fetchData" />
    <Teleport to="#app-left-bar-content">
        <button
            class="mt-4 order-2"
            :disabled="!mappings?.length || mappings.length < 2"
            @click="emit('continue')"
        >
            Continue
        </button>
    </Teleport>
</template>

