<script setup lang="ts">
import { type Job, Status, JobType } from '@/types/job';
import API from '@/utils/api';
import { computed, ref } from 'vue';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

type JobDisplayProps = {
    job: Job;
};

const props = defineProps<JobDisplayProps>();

const emit = defineEmits([ 'deleteJob' ]);

const fetching = ref(false);

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
    fetching.value = true;

    const result = await API.jobs.startJob({ id: props.job.id });
    if (result.status)
        props.job.setStatus(result.data.status);

    fetching.value = false;
}

async function deleteJob() {
    fetching.value = true;

    const result = await API.jobs.deleteJob({ id: props.job.id });
    if (result.status)
        emit('deleteJob');

    fetching.value = false;
}

async function cancelJob() {
    fetching.value = true;

    const result = await API.jobs.cancelJob({ id: props.job.id });
    if (result.status)
        props.job.setStatus(result.data.status);

    fetching.value = false;
}

async function restartJob() {
    fetching.value = true;

    const result = await API.jobs.startJob({ id: props.job.id });
    if (result.status)
        props.job.setStatus(result.data.status);

    fetching.value = false;
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
                v-if="job.type === JobType.JsonLdToCategory && job.dataSource"
                label="Data source:"
            >
                <RouterLink :to="{ name: 'dataSource', params: { id: job.dataSource.id }, query: { categoryId: job.categoryId } }">
                    {{ job.dataSource.label }}
                </RouterLink>
            </ValueRow>
            <ValueRow
                v-else-if="job.logicalModel"
                label="Logical model:"
            >
                <RouterLink :to="{ name: 'logicalModel', params: { id: job.logicalModel.id } }">
                    {{ job.logicalModel.label }}
                </RouterLink>
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
                :disabled="fetching"
                class="success"
                @click="startJob"
            >
                Start
            </button>
            <button
                v-if="job.status !== Status.Running"
                :disabled="fetching"
                class="error"
                @click="deleteJob"
            >
                Delete
            </button>
            <button
                v-if="job.status === Status.Finished || job.status === Status.Canceled"
                :disabled="fetching"
                class="warning"
                @click="restartJob"
            >
                Restart
            </button>
            <button
                v-if="job.status === Status.Running"
                :disabled="fetching"
                class="error"
                @click="cancelJob"
            >
                Cancel
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
