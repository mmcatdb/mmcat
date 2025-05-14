<script setup lang="ts">
import { computed, shallowRef } from 'vue';
import API from '@/utils/api';
import { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import { useWorkflow } from '@/utils/injects';
import type { Id } from '@/types/id';
import { useFixedRouter } from '@/router/specificRoutes';
import type { WorkflowData } from '@/types/workflow';

const workflow = useWorkflow();
const emit = defineEmits([ 'continue' ]);

const allDatasources = shallowRef<Datasource[]>([]);
const datasources = computed(() => {
    if (!allDatasources.value || !workflow.value)
        return;

    const selected: Datasource[] = [];
    const other: Datasource[] = [];
    allDatasources.value.forEach(datasource => (workflow.value.data.inputDatasourceIds.includes(datasource.id) ? selected : other).push(datasource));

    return { selected, other };
});
    
async function fetchDatasources() {
    const result = await API.datasources.getAllDatasources({});
    if (!result.status)
        return false;

    allDatasources.value = result.data.map(Datasource.fromServer);
    return true;
}

const router = useFixedRouter();

function createNewAndSelect() {
    router.push({ name: 'datasource', params: { id: 'new' }, query: { state: 'selecting' } });
}

function edit(id: Id) {
    router.push({ name: 'datasource', params: { id }, query: { state: 'editing' } });
}

async function toggleSelected(id: Id, doSelect: boolean) {
    if (!workflow)
        throw new Error('No workflow');

    const newData: WorkflowData = doSelect
        ? { ...workflow.value.data, inputDatasourceIds: [ ...workflow.value.data.inputDatasourceIds, id ] }
        : { ...workflow.value.data, inputDatasourceIds: workflow.value.data.inputDatasourceIds.filter(dsId => dsId !== id) };
    const result = await API.workflows.updateWorkflowData({ id: workflow.value.id }, newData);
    if (!result.status)
        return;
    
    workflow.value = result.data;
}
</script>

<template>
    <h1>Select input datasource</h1>
    <p>
        Select a datasource as the primary input to the inference process. Choose from existing options or create a new datasource.
    </p>
    <template v-if="datasources && 'selected' in datasources">
        <div class="mt-3 d-flex flex-wrap gap-3 align-items-stretch">
            <button
                class="fs-3 p-5 text-nowrap"
                @click="createNewAndSelect"
            >
                Create new
                <br />
                and select it
            </button>
            <DatasourceDisplay
                v-for="datasource in datasources.selected"
                :key="datasource.id"
                :datasource="datasource"
                scondary-button="Unselect"
                @edit="edit(datasource.id)"
                @on-secondary-click="toggleSelected(datasource.id, false)"
            />
        </div>
        <h1 class="mt-3">
            Available datasources
        </h1>
        <div class="mt-3 d-flex flex-wrap gap-3 align-items-stretch">
            <DatasourceDisplay
                v-for="datasource in datasources.other"
                :key="datasource.id"
                :datasource="datasource"
                scondary-button="Select as input"
                @edit="edit(datasource.id)"
                @on-secondary-click="toggleSelected(datasource.id, true)"
            />
        </div>
    </template>
    <ResourceLoader :loading-function="fetchDatasources" />
    <Teleport to="#app-left-bar-content">
        <button
            class="mt-4 order-2 primary"
            :disabled="!datasources?.selected"
            @click="emit('continue')"
        >
            Continue
        </button>
    </Teleport>
</template>
