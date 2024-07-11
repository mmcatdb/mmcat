<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import { Job, JobState } from '@/types/job';
import { ActionType } from '@/types/action';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import JobStateBadge from './JobStateBadge.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import TextArea from '../input/TextArea.vue';
import InferenceJobDisplay from '@/components/category/inferenceEdit/InferenceJobDisplay.vue'
import type { AbstractInferenceEdit } from '@/types/inferenceEdit/inferenceEdit'
import { SaveJobResultPayload } from '@/types/inferenceEdit/inferenceEdit';
import { SchemaCategory } from '@/types/schema';
import { isInferenceJobData } from '@/utils/InferenceJobData';

type JobDisplayProps = {
    job: Job;
    isShowDetail?: boolean;
};

const props = defineProps<JobDisplayProps>();
const error = computed(() => props.job.error ? { name: props.job.error.name, data: stringify(props.job.error.data) } : undefined);

const result = computed(() => {
    if (props.job.payload.type === ActionType.RSDToCategory) {
        return '';
    }
    return stringify(props.job.result);
});

const resultModel = computed(() => props.job.resultModel);
const showGeneratedDataModel = ref(false);

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

const schemaCategory = computed(() => {
    if (typeof props.job.result === 'string' && props.job.payload.type === ActionType.RSDToCategory) {
        const parsedResult = JSON.parse(props.job.result);
        if (isInferenceJobData(parsedResult)) {
            return SchemaCategory.fromServer(parsedResult.finalSchema, []);
        } else {
            throw new Error("InferenceJobData is not the right type");            
        }
    }
    throw new Error("InferenceJobData is not the right type");
});

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

async function saveJob(edit: AbstractInferenceEdit, permanent: boolean) {
    if (edit != null) {
        console.log("edit is not null in saveJob displayjob");
    }  

    fetching.value = true;

    const saveJobResultPayload = new SaveJobResultPayload(permanent, edit);
    console.log("saveJobResultPayload before sending to server: " + JSON.stringify(saveJobResultPayload));

    const result = await API.jobs.saveJobResult({ id: props.job.id }, { payload: JSON.stringify(saveJobResultPayload) });
    if (result.status) {
        console.log("about to emit updateJob in jobdisplay");
        emit('updateJob', Job.fromServer(result.data));
    }

    fetching.value = false;

}

async function cancelEdit() {
    fetching.value = true;

    const result = await API.jobs.cancelLastJobEdit({ id: props.job.id });
    if (result.status) {
        console.log("about to emit updateJob in jobdisplay");
        emit('updateJob', Job.fromServer(result.data));
    }

    fetching.value = false;

}

function toggleGeneratedDataModel() {
    showGeneratedDataModel.value = !showGeneratedDataModel.value;
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
                    <VersionDisplay :version-id="job.payload.prevVersion" /> --> <VersionDisplay :version-id="job.payload.nextVersion" />
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
                <button
                    v-if="job.payload.type === ActionType.CategoryToModel && isShowDetail"
                    :disabled="fetching"
                    class="secondary"
                    @click="toggleGeneratedDataModel"
                >
                    {{ showGeneratedDataModel ? 'Job output' : 'Generated Model' }}
                </button>
            </div>
        </div>
        <div
            v-if="isShowDetail"
        >
             <template v-if="job.payload.type === ActionType.RSDToCategory && job.state === JobState.Waiting">
                <InferenceJobDisplay 
                    :job="job"
                    :schema-category="schemaCategory"
                    @updateEdit="(edit) => saveJob(edit, false)"
                    @cancel-edit="cancelEdit"
                />
                <div class="d-flex justify-content-end mt-2">
                    <button 
                        v-if="job.payload.type === ActionType.RSDToCategory && job.state === JobState.Waiting && isShowDetail"
                        :disabled="fetching"
                        class="primary"
                        @click="() => saveJob(null, true)"
                    >
                        Save and Finish
                    </button>
                </div>
            </template>
            <TextArea
                v-if="error"
                v-model="error.data"
                class="w-100 mt-2 text-danger"
                readonly
                :min-rows="1"
            />
            <TextArea
                v-else-if="showGeneratedDataModel && resultModel"
                :value="resultModel"
                class="w-100 mt-2"
                readonly
                :min-rows="1"
            />
           <TextArea
                v-else-if="!showGeneratedDataModel && result && job.payload.type !== ActionType.RSDToCategory"
                v-model="result"
                class="w-100 mt-2"
                readonly
                :min-rows="1"
            /> 
        </div>
    </div>
</template>
