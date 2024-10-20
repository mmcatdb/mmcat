<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import { Datasource } from '@/types/datasource';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatasourceDisplay from '@/components/datasource/DatasourceDisplay.vue';
import { tryUseSchemaCategoryId, tryUseWorkflow } from '@/utils/injects';
import type { Id } from '@/types/id';
import { useFixedRouter } from '@/router/specificRoutes';

const categoryId = tryUseSchemaCategoryId();
const workflow = tryUseWorkflow();

const datasources = shallowRef<{
    all: Datasource[];
} | {
    inCategory: Datasource[];
    other: Datasource[];
} | {
    inWorkflow: Datasource[];
    other: Datasource[];
}>();

async function fetchDatasources() {
    if (workflow) {
        const results = await Promise.all([
            API.datasources.getAllDatasources({}),
            API.datasources.getAllDatasources({}, { ids: workflow.value.data.allDatasourceIds }),
        ]);
        if (!results[0].status || !results[1].status)
            return false;

        const inWorkflow = workflow.value.data.allDatasourceIds.length > 0
            ? results[1].data.map(Datasource.fromServer)
            : [];
        const other = results[0].data
            .filter(datasource => !inWorkflow.find(d => d.id === datasource.id))
            .map(Datasource.fromServer);

        datasources.value = { inWorkflow, other };
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

async function addToWorkflow(id: Id) {
    if (!workflow)
        throw new Error('No workflow');

    const newData = { ...workflow.value.data, allDatasourceIds: [ ...workflow.value.data.allDatasourceIds, id ] };
    const result = await API.workflows.updateWorkflowData({ id: workflow.value.id }, newData);
    if (!result.status)
        return;

    if (!datasources.value || !('inWorkflow' in datasources.value))
        throw new Error('No workflow');

    workflow.value = result.data;

    const datasource = datasources.value.other.find(datasource => datasource.id === id);
    if (!datasource)
        return;

    datasources.value = {
        inWorkflow: [ ...datasources.value.inWorkflow, datasource ],
        other: datasources.value.other.filter(datasource => datasource.id !== id),
    };
}

async function removeFromWorkflow(id: Id) {
    if (!workflow)
        throw new Error('No workflow');

    const newData = { ...workflow.value.data, allDatasourceIds: workflow.value.data.allDatasourceIds.filter(dId => dId !== id) };
    const result = await API.workflows.updateWorkflowData({ id: workflow.value.id }, newData);
    if (!result.status)
        return;

    if (!datasources.value || !('inWorkflow' in datasources.value))
        throw new Error('No workflow');

    workflow.value = result.data;

    const datasource = datasources.value.inWorkflow.find(datasource => datasource.id === id);
    if (!datasource)
        return;

    datasources.value = {
        inWorkflow: datasources.value.inWorkflow.filter(datasource => datasource.id !== id),
        other: [ ...datasources.value.other, datasource ],
    };
}
</script>

<template>
    <div>
        <!-- Only in a specific workflow. -->
        <template v-if="workflow">
            <h1>Datasources in workflow</h1>
            <template v-if="datasources && 'inWorkflow' in datasources">
                <div class="d-flex flex-wrap mt-3">
                    <div
                        v-for="datasource in datasources.inWorkflow"
                        :key="datasource.id"
                    >
                        <DatasourceDisplay
                            :datasource="datasource"
                            button="Remove from workflow"
                            @edit="edit(datasource.id)"
                            @on-click="removeFromWorkflow(datasource.id)"
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
                            button="Add to workflow"
                            @edit="edit(datasource.id)"
                            @on-click="addToWorkflow(datasource.id)"
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
