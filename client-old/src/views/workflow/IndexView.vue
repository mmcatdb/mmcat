<script setup lang="ts">
import { useWorkflow } from '@/utils/injects';
import API from '@/utils/api';
import DatasourcesView from './DatasourcesView.vue';
import JobView from './JobView.vue';
import AddMappingsView from './AddMappingsView.vue';
import ResultsView from './ResultsView.vue';

const workflow = useWorkflow();

async function continueWorkflow() {
    const result = await API.workflows.continueWorkflow({ id: workflow.value.id });
    if (!result.status) 
        return;

    workflow.value = result.data;
}
</script>

<template>
    <DatasourcesView
        v-if="workflow.data.step === 'selectInput'"
        @continue="continueWorkflow"
    />
    <JobView
        v-else-if="workflow.data.step === 'editCategory'"
        @continue="continueWorkflow"
    />
    <AddMappingsView
        v-else-if="workflow.data.step === 'addMappings'"
        @continue="continueWorkflow"
    />
    <ResultsView v-else />
</template>
