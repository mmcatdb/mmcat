<script setup lang="ts">
import { type Job, Status, JobType } from '@/types/job';
import API from '@/utils/api';
import { computed, ref } from 'vue';
import CleverRouterLink from '@/components/CleverRouterLink.vue';

interface JobDisplayProps {
    job: Job;
}

const props = defineProps<JobDisplayProps>();

const emit = defineEmits([ 'deleteJob' ]);

const startJobDisabled = ref(false);
const deleteJobDisabled = ref(false);
const restartJobDisabled = ref(false);

const jobStatusClass = computed(() => {
    switch (props.job.status) {
    case Status.Canceled:
        return 'text-error';
    case Status.Finished:
        return 'text-success';
    default:
        return '';
    }
});

async function startJob() {
    startJobDisabled.value = true;

    const result = await API.jobs.startJob({ id: props.job.id });
    if (result.status)
        props.job.setStatus(result.data.status);

    startJobDisabled.value = false;
}

async function deleteJob() {
    deleteJobDisabled.value = true;

    const result = await API.jobs.deleteJob({ id: props.job.id });
    if (result.status)
        emit('deleteJob');

    deleteJobDisabled.value = false;
}

async function restartJob() {
    restartJobDisabled.value = true;

    const result = await API.jobs.startJob({ id: props.job.id });
    if (result.status)
        props.job.setStatus(result.data.status);

    restartJobDisabled.value = false;
}
</script>

<template>
    <div class="job-display">
        <CleverRouterLink :to="{ name: 'job', params: { id: job.id } }">
            <h2>{{ job.label }}</h2>
        </CleverRouterLink>
        <table>
            <tr>
                <td class="label">
                    Id:
                </td>
                <td class="value">
                    {{ job.id }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    {{ job.type }}
                </td>
            </tr>
            <tr v-if="job.type === JobType.JsonLdToCategory">
                <td class="label">
                    Data source id:
                </td>
                <td class="value">
                    {{ job.dataSourceId }}
                </td>
            </tr>
            <tr v-else>
                <td class="label">
                    Logical model id:
                </td>
                <td class="value">
                    {{ job.logicalModelId }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Status:
                </td>
                <td
                    :class="jobStatusClass"
                    class="value"
                >
                    {{ job.status }}
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                v-if="job.status === Status.Ready"
                :disabled="startJobDisabled"
                class="success"
                @click="startJob"
            >
                Start
            </button>
            <button
                v-if="job.status !== Status.Running"
                :disabled="deleteJobDisabled"
                class="error"
                @click="deleteJob"
            >
                Delete
            </button>
            <button
                v-if="job.status === Status.Finished || job.status === Status.Canceled"
                :disabled="restartJobDisabled"
                class="warning"
                @click="restartJob"
            >
                Restart
            </button>
        </div>
    </div>
</template>

<style scoped>
.job-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 284px;
}
</style>
