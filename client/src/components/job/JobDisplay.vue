<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import { Job, JobState } from '@/types/job';
import { ActionType } from '@/types/action';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import JobStateBadge from './JobStateBadge.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';

type JobDisplayProps = {
    job: Job;
};

const props = defineProps<JobDisplayProps>();

const emit = defineEmits<{
    (e: 'updateJob', job: Job): void;
}>();

const fetching = ref(false);

async function startJob() {
    fetching.value = true;

    const result = await API.jobs.startJob({ id: props.job.id });
    if (result.status)
        emit('updateJob', Job.fromServer(result.data));

    fetching.value = false;
}

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
    <div class="d-flex gap-3 align-items-start border border-primary px-3 py-2">
        <div>
            <div style="line-height: 25.6px;">
                <JobStateBadge :state="job.state" />
            </div>
            <div class="text-muted small text-nowrap monospace-numbers">
                {{ job.createdAt.toLocaleString(undefined, { month: '2-digit', day: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) }}
            </div>
        </div>
        <div class="col-4 d-flex align-items-center gap-3">
            <div>
                <CleverRouterLink :to="{ name: 'job', params: { id: job.id } }">
                    <div class="fs-6 fw-bold">
                        {{ job.label }}
                    </div>
                </CleverRouterLink>
                <div class="text-muted small">
                    {{ job.id }}
                </div>
            </div>
        </div>
        <div>
            {{ job.payload.type }}
        </div>
        <div class="col-3">
            <template v-if="job.payload.type === ActionType.RSDToCategory && job.payload.dataSource">
                <RouterLink :to="{ name: 'dataSource', params: { id: job.payload.dataSource.id }, query: { categoryId: job.categoryId } }">
                    {{ job.payload.dataSource.label }}
                </RouterLink>
            </template>
            <template v-else-if="job.payload.type === ActionType.RSDToCategory && job.payload.database">
                <RouterLink :to="{ name: 'database', params: { id: job.payload.database.id } }">
                    {{ job.payload.database.label }}
                </RouterLink>
            </template>
            <template v-else-if="job.payload.type === ActionType.JsonLdToCategory">
                <RouterLink :to="{ name: 'dataSource', params: { id: job.payload.dataSource.id }, query: { categoryId: job.categoryId } }">
                    {{ job.payload.dataSource.label }}
                </RouterLink>
            </template>
            <template v-else-if="job.payload.type === ActionType.CategoryToModel || job.payload.type === ActionType.ModelToCategory">
                <RouterLink :to="{ name: 'logicalModel', params: { id: job.payload.logicalModel.id } }">
                    {{ job.payload.logicalModel.label }}
                </RouterLink>
            </template>
            <template v-else>
                <VersionDisplay :version-id="job.payload.prevVersion" /> --> <VersionDisplay :version-id="job.payload.nextVersion" />
            </template>
        </div>
        <div class="flex-grow-1">
            <template v-if="job.state === JobState.Failed && job.data">
                Error: {{ job.data.name }}
            </template>
            <template v-else>
                &nbsp;
            </template>
        </div>
        <div class="d-flex gap-3 align-self-center">
            <button
                v-if="job.state === JobState.Paused"
                :disabled="fetching"
                class="success"
                @click="startJob"
            >
                Start
            </button>
            <button
                v-if="job.state === JobState.Paused || job.state === JobState.Ready"
                :disabled="fetching"
                class="warning"
                @click="cancelJob"
            >
                Cancel
            </button>
            <button
                v-if="job.state === JobState.Finished || job.state === JobState.Failed || job.state === JobState.Canceled"
                :disabled="fetching"
                class="info"
                @click="restartJob"
            >
                Restart
            </button>
        </div>
    </div>
</template>
