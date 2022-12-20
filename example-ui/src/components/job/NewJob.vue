<script setup lang="ts">
import { defineEmits, onMounted, ref } from 'vue';
import { JOB_TYPES, JobType } from '@/types/job';
import API from '@/utils/api';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';

const emit = defineEmits([ 'newJob' ]);

const logicalModels = ref<LogicalModel[]>();
const fetched = ref(false);
const logicalModelId = ref<number>();
const jobName = ref<string>('');
const jobType = ref<JobType>();
const fetching = ref(false);

const schemaCategoryId = useSchemaCategory();

onMounted(async () => {
    const result = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId: schemaCategoryId });
    if (result.status)
        logicalModels.value = result.data.map(LogicalModel.fromServer);

    fetched.value = true;
});

async function createJob() {
    if (!logicalModelId.value || !jobType.value)
        return;

    fetching.value = true;

    const result = await API.jobs.createNewJob({}, {
        logicalModelId: logicalModelId.value,
        label: jobName.value,
        type: jobType.value
    });
    if (result.status)
        emit('newJob', result.data);

    fetching.value = false;
}
</script>

<template>
    <div class="newJob">
        <h2>Create a new job</h2>
        <table>
            <tr>
                <td class="label">
                    Label:
                </td>
                <td class="value">
                    <input v-model="jobName" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    <select v-model="jobType">
                        <option
                            v-for="availableType in JOB_TYPES"
                            :key="availableType.value"
                            :value="availableType.value"
                        >
                            {{ availableType.label }}
                        </option>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="label">
                    Logical model:
                </td>
                <td class="value">
                    <select v-model="logicalModelId">
                        <option
                            v-for="logicalModel in logicalModels"
                            :key="logicalModel.id"
                            :value="logicalModel.id"
                        >
                            {{ logicalModel.label }}
                        </option>
                    </select>
                </td>
            </tr>
            <tr>
                &nbsp;<!-- To make the NewJob tile look the same as the JobDisplay tile. -->
            </tr>
        </table>
        <div class="button-row">
            <button
                :disabled="(fetching || !jobName || !jobType || !logicalModelId)"
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
