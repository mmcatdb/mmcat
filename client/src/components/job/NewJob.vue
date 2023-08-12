<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { JOB_TYPES, JobType, type JobPayloadInit } from '@/types/job';
import API from '@/utils/api';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import { DataSource } from '@/types/dataSource';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

const emit = defineEmits([ 'newJob' ]);

const logicalModels = ref<LogicalModel[]>();
const dataSources = ref<DataSource[]>();
const fetched = ref(false);
const logicalModelId = ref<Id>();
const dataSourceId = ref<Id>();
const jobName = ref<string>('');
const jobType = ref(JOB_TYPES[0].value);
const fetching = ref(false);

const categoryId = useSchemaCategoryId();

onMounted(async () => {
    const logicalModelResult = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (logicalModelResult.status)
        logicalModels.value = logicalModelResult.data.map(LogicalModel.fromServer);

    const dataSourceResult = await API.dataSources.getAllDataSources({});
    if (dataSourceResult.status)
        dataSources.value = dataSourceResult.data.map(DataSource.fromServer);

    fetched.value = true;
});

const dataValid = computed(() => {
    if (!jobName.value)
        return false;

    return jobType.value === JobType.JsonLdToCategory
        ? !!dataSourceId.value
        : !!logicalModelId.value;
});

async function createJob() {
    fetching.value = true;

    const payload = jobType.value === JobType.JsonLdToCategory ? {
        type: JobType.JsonLdToCategory,
        dataSourceId: dataSourceId.value,
    } : {
        type: jobType.value,
        logicalModelId: logicalModelId.value,
    };

    const result = await API.jobs.createNewJob({}, {
        categoryId,
        label: jobName.value,
        payload: payload as JobPayloadInit,
    });
    if (result.status)
        emit('newJob', result.data);

    fetching.value = false;
}
</script>

<template>
    <div class="newJob">
        <h2>Create a new job</h2>
        <ValueContainer>
            <ValueRow label="Type:">
                <select v-model="jobType">
                    <option
                        v-for="availableType in JOB_TYPES"
                        :key="availableType.value"
                        :value="availableType.value"
                    >
                        {{ availableType.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow label="Label:">
                <input v-model="jobName" />
            </ValueRow>
            <ValueRow
                v-if="jobType === JobType.JsonLdToCategory"
                label="Data source:"
            >
                <select v-model="dataSourceId">
                    <option
                        v-for="dataSource in dataSources"
                        :key="dataSource.id"
                        :value="dataSource.id"
                    >
                        {{ dataSource.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow
                v-else
                label="Logical model:"
            >
                <select v-model="logicalModelId">
                    <option
                        v-for="logicalModel in logicalModels"
                        :key="logicalModel.id"
                        :value="logicalModel.id"
                    >
                        {{ logicalModel.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow>&nbsp;</ValueRow><!-- To make the NewJob tile look the same as the JobDisplay tile. -->
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="(fetching || !dataValid)"
                @click="createJob"
            >
                Create job
            </button>
        </div>
    </div>
</template>

<style scoped>
.newJob {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 284px;
}
</style>
