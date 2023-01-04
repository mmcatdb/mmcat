<script setup lang="ts">
import { type Job, Status, JobType } from '@/types/job';
import API from '@/utils/api';
import { computed, ref } from 'vue';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

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
        <ValueContainer>
            <ValueRow label="Id:">
                {{ job.id }}
            </ValueRow>
            <ValueRow label="Type:">
                {{ job.type }}
            </ValueRow>
            <ValueRow
                v-if="job.type === JobType.JsonLdToCategory"
                label="Data source id:"
            >
                {{ job.dataSourceId }}
            </ValueRow>
            <ValueRow
                v-else
                label="Logical model id:"
            >
                {{ job.logicalModelId }}
            </ValueRow>
            <ValueRow label="Status:">
                <span :class="jobStatusClass">
                    {{ job.status }}
                </span>
            </ValueRow>
        </ValueContainer>
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
