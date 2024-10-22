<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import { useWorkflow } from '@/utils/injects';
import type { Id } from '@/types/id';
import { useFixedRouter } from '@/router/specificRoutes';
import { type WorkflowData } from '@/types/workflow';

const workflow = useWorkflow();
const emit = defineEmits([ 'continue' ]);

const datasources = shallowRef<{
    all: Datasource[];
    selected?: Datasource;
    other: Datasource[];
}>();

async function fetchDatasources() {
    const result = await API.datasources.getAllDatasources({});
    if (!result.status)
        return false;

    const all = result.data.map(Datasource.fromServer);
    const selected = workflow.value.data.inputDatasourceId
        ? all.find(datasource => datasource.id === workflow.value.data.inputDatasourceId)
        : undefined;
    const other = workflow.value.data.inputDatasourceId
        ? all.filter(datasource => datasource.id !== workflow.value.data.inputDatasourceId)
        : all;

    datasources.value = { all, selected, other };
    return true;
}

const router = useFixedRouter();

function createNewAndSelect() {
    router.push({ name: 'datasource', params: { id: 'new' }, query: { state: 'selecting' } });
}

function edit(id: Id) {
    router.push({ name: 'datasource', params: { id }, query: { state: 'editing' } });
}

async function selectAsInput(id: Id) {
    if (!workflow)
        throw new Error('No workflow');

    const newData: WorkflowData = { ...workflow.value.data, inputDatasourceId: id };
    const result = await API.workflows.updateWorkflowData({ id: workflow.value.id }, newData);
    if (!result.status)
        return;
    
    workflow.value = result.data;
    
    if (!datasources.value || !('selected' in datasources.value))
        throw new Error('No workflow');

    const selected = datasources.value.all.find(datasource => datasource.id === id);
    if (!selected)
        return;

    datasources.value = {
        all: datasources.value.all,
        selected,
        other: datasources.value.all.filter(datasource => datasource.id !== id),
    };
}
</script>

<template>
    <h1>Select input datasource</h1>
    <p>
        This datasource will be used as ... TODO
    </p>
    <template v-if="datasources && 'selected' in datasources">
        <div class="d-flex mt-3 gap-3 align-items-stretch">
            <DatasourceDisplay
                v-if="datasources.selected"
                :datasource="datasources.selected"
                @edit="edit(datasources.selected.id)"
            />
            <button
                class="fs-3 p-5"
                @click="createNewAndSelect"
            >
                Create new
                <br />
                and select it
            </button>
        </div>
        <h1 class="mt-3">
            Available datasources
        </h1>
        <div class="d-flex flex-wrap mt-3 gap-3 align-items-stretch">
            <DatasourceDisplay
                v-for="datasource in datasources.other"
                :key="datasource.id"
                :datasource="datasource"
                scondary-button="Select as input"
                @edit="edit(datasource.id)"
                @on-secondary-click="selectAsInput(datasource.id)"
            />
        </div>
    </template>
    <ResourceLoader :loading-function="fetchDatasources" />
    <Teleport to="#app-left-bar-content">
        <button
            class="mt-4 order-2"
            :disabled="!datasources?.selected"
            @click="emit('continue')"
        >
            Continue
        </button>
    </Teleport>
</template>
