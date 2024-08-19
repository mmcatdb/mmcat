<script setup lang="ts">
import { computed, ref } from 'vue';
import API from '@/utils/api';
import { Job, JobState, type ModelJobData } from '@/types/job';
import { ActionType } from '@/types/action';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import JobStateBadge from './JobStateBadge.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import TextArea from '../input/TextArea.vue';
import InferenceJobDisplay from '@/components/category/inference/InferenceJobDisplay.vue';
import type { InferenceEdit, SaveJobResultPayload } from '@/types/inference/inferenceEdit';
import { createInferenceEditFromServer } from '@/types/inference/inferenceEdit';
import type { InferenceJobData } from '@/types/inference/InferenceJobData';
import { LayoutType } from '@/types/inference/layoutType';
import { useSchemaCategoryInfo } from '@/utils/injects';

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

//TODO: do I need to create serialized Inference Edits? - Yes
const inferenceEdits = computed(() => {
    if (inferenceJobData.value && inferenceJobData.value.edits.length > 0) 
        return inferenceJobData.value.edits.map(createInferenceEditFromServer);
    else
        return [];
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

async function updateJobResult(edit: InferenceEdit | null, permanent: boolean | null, newLayoutType: LayoutType | null) {
    fetching.value = true;

    const payload: SaveJobResultPayload = { isFinal: permanent, edit, newLayoutType };
    console.log('Sending payload:', JSON.stringify(payload));

    const result = await API.jobs.updateJobResult({ id: props.job.id }, { isFinal: permanent, edit, newLayoutType } as SaveJobResultPayload);
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
                    <CleverRouterLink :to="{name: 'job', params: { id: job.id } }">
                        <div class="fs-6 fw-bold">
                            {{ job.label }}
                        </div>
                    </CleverRouterLink>
                    <div class="text-secondary small">
                        {{ job.id }}
                    </div>
                </div>
            </div>
            <div>
                {{ job.payload.type }}
            </div>
            <div class="col-3">
                <template v-if="job.payload.type === ActionType.RSDToCategory">
                    <RouterLink :to="{ name: 'datasource', params: { id: job.payload.datasource.id }, query: { categoryId: job.categoryId } }">
                        {{ job.payload.datasource.label }}
                    </RouterLink>
                </template>
                <template v-else-if="job.payload.type === ActionType.CategoryToModel || job.payload.type === ActionType.ModelToCategory">
                    <RouterLink :to="{ name: 'logicalModel', params: { id: job.payload.logicalModel.id } }">
                        {{ job.payload.logicalModel.label }}
                    </RouterLink>
                </template>
                <template v-else>
                    <VersionDisplay :version-id="job.payload.prevVersion" /> -> <VersionDisplay :version-id="job.payload.nextVersion" />
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
                    :inference-edits="inferenceEdits"
                    :layout-type="inferenceJobData?.layoutType"
                    :candidates="inferenceJobData?.candidates"
                    @update-edit="(edit) => updateJobResult(edit, false, null)"
                    @cancel-edit="updateJobResult(null, false, null)"
                    @change-layout="(newLayoutType) => updateJobResult(null, null, newLayoutType)"
                >
                    <template #below-editor>
                        <div class="d-flex justify-content-end mt-2">
                            <button 
                                :disabled="fetching"
                                class="primary"
                                @click="() => updateJobResult(null, true, null)"
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
                v-model="(job.data as ModelJobData).model"
                class="w-100 mt-2"
                readonly
                :min-rows="1"
            /> 
        </div>
    </div>
</template>