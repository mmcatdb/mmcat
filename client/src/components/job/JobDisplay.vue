<script setup lang="ts">
import { computed, ref } from 'vue';
import API from '@/utils/api';
import { Job, JobState } from '@/types/job';
import { ActionType } from '@/types/action';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

type JobDisplayProps = {
    job: Job;
};

const props = defineProps<JobDisplayProps>();

const emit = defineEmits<{
    (e: 'updateJob', job: Job): void;
}>();

const fetching = ref(false);

const jobStateClass = computed(() => {
    switch (props.job.state) {
    case JobState.Running:
        return 'text-info';
    case JobState.Failed:
        return 'text-error';
    case JobState.Canceled:
        return 'text-warning';
    case JobState.Finished:
        return 'text-success';
    default:
        return '';
    }
});

async function cancelJob() {
    fetching.value = true;

    const result = await API.jobs.cancelJob({ id: props.job.id });
    if (result.status)
        emit('updateJob', Job.fromServer(result.data));

    fetching.value = false;
}

async function restartJob() {
    fetching.value = true;

    const result = await API.jobs.createRestartedJob({ id: props.job.id });
    if (result.status)
        emit('updateJob', Job.fromServer(result.data));

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
                {{ job.payload.type }}
            </ValueRow>
            <ValueRow
                v-if="job.payload.type === ActionType.JsonLdToCategory"
                label="Data source:"
            >
                <RouterLink :to="{ name: 'dataSource', params: { id: job.payload.dataSource.id }, query: { categoryId: job.categoryId } }">
                    {{ job.payload.dataSource.label }}
                </RouterLink>
            </ValueRow>
            <ValueRow
                v-else
                label="Logical model:"
            >
                <RouterLink :to="{ name: 'logicalModel', params: { id: job.payload.logicalModel.id } }">
                    {{ job.payload.logicalModel.label }}
                </RouterLink>
            </ValueRow>
            <ValueRow label="State:">
                <span :class="jobStateClass">
                    {{ job.state }}
                </span>
            </ValueRow>
            <ValueRow
                v-if="job.state === JobState.Failed && job.data"
                label="Error:"
            >
                {{ job.data.name }}
            </ValueRow>
            <ValueRow v-else>
                &nbsp;
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="fetching"
                class="info"
                @click="restartJob"
            >
                Restart
            </button>
            <button
                v-if="job.state === JobState.Ready"
                :disabled="fetching"
                class="warning"
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
