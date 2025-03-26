<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import { useWorkflow } from '@/utils/injects';
import { useFixedRouter } from '@/router/specificRoutes';
import { Mapping } from '@/types/mapping';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import { Datasource } from '@/types/datasource';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';

const workflow = useWorkflow();
const emit = defineEmits([ 'continue' ]);

const datasources = shallowRef<Datasource[]>();
const mappings = shallowRef<{
    output: Mapping[];
    input: Mapping[];
}>();

async function fetchData() {
    const datasourcesResult = await API.datasources.getAllDatasources({});
    if (!datasourcesResult.status)
        return false;

    const mappingsResult = await API.mappings.getAllMappingsInCategory({}, { categoryId: workflow.value.categoryId });
    if (!mappingsResult.status)
        return false;

    const data = workflow.value.data;
    if (data.step !== 'addMappings')
        return false;
    
    datasources.value = datasourcesResult.data.map(Datasource.fromServer);

    const allMappings = mappingsResult.data.map(Mapping.fromServer);
    mappings.value = {
        output: allMappings.filter(mapping => !data.inputMappingIds.includes(mapping.id)),
        input: allMappings.filter(mapping => data.inputMappingIds.includes(mapping.id)),
    };

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
        Create output mappings. Start fresh with a new mapping or modify an existing one to fit your needs. At least one mapping is needed to proceed.
    </p>
    <p>
        Need to create a new datasource?
        <FixedRouterLink :to="{ name: 'workflow-datasources' }">
            Go to datasources
        </FixedRouterLink>
        .
    </p>
    <template v-if="datasources && mappings">
        <h2>Outputs</h2>
        <div class="mt-3 d-flex flex-wrap gap-3">
            <MappingDisplay
                v-for="mapping in mappings.output"
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
        <h2 class="mt-3">
            Inputs
        </h2>
        <div class="mt-3 d-flex flex-wrap gap-3">
            <MappingDisplay
                v-for="mapping in mappings.input"
                :key="mapping.id"
                :mapping="mapping"
                max-height-path
            />
        </div>
    </template>
    <ResourceLoader :loading-function="fetchData" />
    <Teleport to="#app-left-bar-content">
        <button
            class="mt-4 order-2 primary"
            :disabled="!mappings?.output.length"
            @click="emit('continue')"
        >
            Continue
        </button>
    </Teleport>
</template>

