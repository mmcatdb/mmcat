<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import { tryUseSchemaCategoryId, tryUseWorkflow } from '@/utils/injects';
import type { Id } from '@/types/id';
import { useFixedRouter } from '@/router/specificRoutes';
import { type WorkflowData } from '@/types/workflow';

const categoryId = tryUseSchemaCategoryId();
const workflow = tryUseWorkflow();

const datasources = shallowRef<{
    all: Datasource[];
} | {
    inCategory: Datasource[];
    other: Datasource[];
} | {
    all: Datasource[];
    selected?: Datasource;
    other: Datasource[];
}>();

async function fetchDatasources() {
    if (workflow) {
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

    if (categoryId) {
        const results = await Promise.all([
            API.datasources.getAllDatasources({}),
            API.datasources.getAllDatasources({}, { categoryId }),
        ]);
        if (!results[0].status || !results[1].status)
            return false;

        const inCategory = results[1].data.map(Datasource.fromServer);
        const other = results[0].data
            .filter(datasource => !inCategory.find(d => d.id === datasource.id))
            .map(Datasource.fromServer);

        datasources.value = { inCategory, other };
        return true;
    }


    const result = await API.datasources.getAllDatasources({});
    if (!result.status)
        return false;

    const all = result.data.map(Datasource.fromServer);

    datasources.value = { all };
    return true;
}

const router = useFixedRouter();

function createNew() {
    router.push({ name: 'datasource', params: { id: 'new' } });
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

    if (!datasources.value || !('selected' in datasources.value))
        throw new Error('No workflow');

    workflow.value = result.data;

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
    <div>
        <!-- Only in a specific workflow. -->
        <template v-if="workflow">
            <h1>Datasources in workflow</h1>
            <template v-if="datasources && 'selected' in datasources">
                <div class="d-flex flex-wrap mt-3">
                    <div
                        v-if="datasources.selected"
                    >
                        <DatasourceDisplay
                            :datasource="datasources.selected"
                            @edit="edit(datasources.selected.id)"
                        />
                    </div>
                </div>
                <div class="button-row">
                    <button @click="createNew">
                        Create new
                    </button>
                </div>
                <h1>Other datasources</h1>
                <div class="d-flex flex-wrap mt-3">
                    <div
                        v-for="datasource in datasources.other"
                        :key="datasource.id"
                    >
                        <DatasourceDisplay
                            :datasource="datasource"
                            button="Select as input"
                            @edit="edit(datasource.id)"
                            @on-click="selectAsInput(datasource.id)"
                        />
                    </div>
                </div>
            </template>
        </template>

        <!-- Only in a specific category (but not in workflow). -->
        <template v-else-if="categoryId">
            <h1>Datasources in category</h1>
            <template v-if="datasources && 'inCategory' in datasources">
                <div class="d-flex flex-wrap mt-3">
                    <div
                        v-for="datasource in datasources.inCategory"
                        :key="datasource.id"
                    >
                        <DatasourceDisplay
                            :datasource="datasource"
                            @edit="edit(datasource.id)"
                        />
                    </div>
                </div>
                <div class="button-row">
                    <button @click="createNew">
                        Create new
                    </button>
                </div>
                <h1>Other datasources</h1>
                <div class="d-flex flex-wrap mt-3">
                    <div
                        v-for="datasource in datasources.other"
                        :key="datasource.id"
                    >
                        <DatasourceDisplay
                            :datasource="datasource"
                            @edit="edit(datasource.id)"
                        />
                    </div>
                </div>
            </template>
        </template>

        <!-- All datasources. -->
        <template v-else>
            <h1>Datasources</h1>
            <template v-if="datasources && 'all' in datasources">
                <div class="d-flex flex-wrap mt-3">
                    <div
                        v-for="datasource in datasources.all"
                        :key="datasource.id"
                    >
                        <DatasourceDisplay
                            :datasource="datasource"
                            @edit="$router.push({ name: 'datasource', params: { id: datasource.id }, query: { state: 'editing' } });"
                        />
                    </div>
                </div>
                <div class="button-row">
                    <button @click="createNew">
                        Create new
                    </button>
                </div>
            </template>
        </template>

        <ResourceLoader :loading-function="fetchDatasources" />
    </div>
</template>
