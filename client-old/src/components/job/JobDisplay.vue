<script setup lang="ts">
import { computed, ref } from 'vue';
import API from '@/utils/api';
import { Job, JobState, type ModelJobData } from '@/types/job';
import { ActionType } from '@/types/action';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import JobStateBadge from './JobStateBadge.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import TextArea from '../input/TextArea.vue';
import InferenceJobDisplay from '@/components/category/inference/InferenceJobDisplay.vue';
import type { InferenceEdit, SaveJobResultPayload } from '@/types/inference/inferenceEdit';
import type { InferenceJobData } from '@/types/inference/InferenceJobData';
import type { LayoutType } from '@/types/inference/layoutType';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { Key } from '@/types/identifiers';
import type { Position } from 'cytoscape';

type JobDisplayProps = {
    job: Job;
    isShowDetail?: boolean;
};

const props = defineProps<JobDisplayProps>();
const error = computed(() => props.job.error ? { name: props.job.error.name, data: stringify(props.job.error.data) } : undefined);

function stringify(value: unknown): string | undefined {
    if (value === undefined || value === null)
        return undefined;

    if (typeof value === 'object')
        return JSON.stringify(value, null, 4);

    return '' + value;
}

const emit = defineEmits<{
    (e: 'updateJob', job: Job): void;
}>();

const fetching = ref(false);

const inferenceJobData = computed(() => {
    if (props.job.payload.type === ActionType.RSDToCategory) 
        return props.job.data as InferenceJobData;
    else
        throw new Error('Expected job payload type to be RSDToCategory, but got ' + props.job.payload.type);
}); 

const info = useSchemaCategoryInfo();

async function startJob() {
    fetching.value = true;
    const result = await API.jobs.startJob({ id: props.job.id });
    fetching.value = false;
    if (result.status)
        emit('updateJob', Job.fromServer(result.data, info.value));
}

async function cancelJob() {
    fetching.value = true;
    const result = await API.jobs.cancelJob({ id: props.job.id });
    fetching.value = false;
    if (result.status)
        emit('updateJob', Job.fromServer(result.data, info.value));
}

async function restartJob() {
    fetching.value = true;
    const result = await API.jobs.createRestartedJob({ id: props.job.id });
    fetching.value = false;
    if (result.status)
        emit('updateJob', Job.fromServer(result.data, info.value));
}

async function updateJobResult(edit: InferenceEdit | null, isFinal: boolean | null, layoutType: LayoutType | null, positionsMap: Map<Key, Position> | null) {
    fetching.value = true;

    const positions = positionsMap
        ? Array.from(positionsMap.entries()).map(([key, position]) => ({
            key: key,
            position,
        }))
        : null;

    const payload: SaveJobResultPayload = { isFinal, edit, layoutType, positions };
    const result = await API.jobs.updateJobResult({ id: props.job.id }, payload);
    fetching.value = false;
    if (result.status) 
        emit('updateJob', Job.fromServer(result.data, info.value));
}
</script>

<template>
    <div class="border border-primary px-3 py-2">
        <div class="d-flex gap-3 align-items-start">
            <div>
                <div style="line-height: 25.6px;">
                    <JobStateBadge :state="job.state" />
                </div>
                <div class="text-secondary small text-nowrap monospace-numbers">
                    {{ job.createdAt.toLocaleString(undefined, { month: '2-digit', day: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) }}
                </div>
            </div>
            <div class="col-4 d-flex align-items-center gap-3">
                <div>
                    <FixedRouterLink :to="{ name: 'job', params: { id: job.id } }">
                        <div class="fs-6 fw-bold">
                            {{ job.label }}
                        </div>
                    </FixedRouterLink>
                    <div class="text-secondary small">
                        {{ job.id }}
                    </div>
                </div>
            </div>
            <div>
                {{ job.payload.type }}
            </div>
            <div class="col-3">
                <template v-if="job.payload.type === ActionType.UpdateSchema">
                    <VersionDisplay :version-id="job.payload.prevVersion" /> -> <VersionDisplay :version-id="job.payload.nextVersion" />
                </template>
                <div v-else-if="job.payload.type === ActionType.CategoryToModel || job.payload.type === ActionType.ModelToCategory">
                    <FixedRouterLink :to="{ name: 'datasource', params: { id: job.payload.datasource.id } }">
                        {{ job.payload.datasource.label }}
                    </FixedRouterLink>
                    <div
                        v-if="job.payload.mappings"
                        class="d-flex flex-wrap"
                    >
                        <span
                            v-for="(mapping, index) in job.payload.mappings"
                            :key="mapping.id"
                        >
                            <FixedRouterLink 
                                :to="{ name: 'mapping', params: {id: mapping.id } }"
                            >
                                {{ mapping.kindName }}
                            </FixedRouterLink>
                            <span
                                v-if="index !== job.payload.mappings.length - 1"
                                class="px-1"
                            >,</span>
                        </span>
                    </div>
                </div>
                <template v-else>
                    <FixedRouterLink
                        v-for="datasource in job.payload.datasources"
                        :key="datasource.id"
                        :to="{ name: 'datasource', params: { id: datasource.id } }"
                    >
                        {{ datasource.label }}
                    </FixedRouterLink>
                </template>
            </div>
            <div class="flex-grow-1">
                <div
                    v-if="job.error"
                    class="text-danger"
                >
                    Error: {{ job.error.name }}
                </div>
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
        <div
            v-if="isShowDetail"
        >
            <template v-if="job.payload.type === ActionType.RSDToCategory && job.state === JobState.Waiting">
                <InferenceJobDisplay 
                    :job="job"
                    :schema-category="inferenceJobData?.finalSchema"
                    :inference-edits="inferenceJobData?.edits"
                    :layout-type="inferenceJobData?.layoutType"
                    :candidates="inferenceJobData?.candidates"
                    @update-edit="(edit) => updateJobResult(edit, false, null, null)"
                    @cancel-edit="updateJobResult(null, false, null, null)"
                    @change-layout="(newLayoutType) => updateJobResult(null, null, newLayoutType, null)"
                    @save-positions="(map) => updateJobResult(null, false, null, map)"
                >
                    <template #below-editor>
                        <div class="d-flex justify-content-end mt-2">
                            <button 
                                :disabled="fetching"
                                class="primary"
                                @click="() => updateJobResult(null, true, null, null)"
                            >
                                Save and Finish
                            </button>
                        </div>
                    </template>
                </InferenceJobDisplay>
            </template>
            <TextArea
                v-if="error"
                v-model="error.data"
                class="w-100 mt-2 text-danger"
                readonly
                :min-rows="1"
            />
            <TextArea
                v-else-if="job.payload.type !== ActionType.RSDToCategory && job.data"
                v-model="(job.data as ModelJobData).value"
                class="w-100 mt-2"
                readonly
                :min-rows="1"
            /> 
        </div>
    </div>
</template>