<script setup lang="ts">
import { ref } from 'vue';
import { Job, JobState } from '@/types/job';
import API from '@/utils/api';
import { useSchemaCategoryInfo, useWorkflow } from '@/utils/injects';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';

const workflow = useWorkflow();
const emit = defineEmits([ 'continue' ]);

const job = ref<Job>();
const info = useSchemaCategoryInfo();

async function fetchJob() {
    const data = workflow.value.data;
    if (data.step !== 'editCategory')
        throw new Error('Invalid step "' + data.step + '" for JobView.');

    const result = await API.jobs.getJob({ id: data.inferenceJobId });
    if (!result.status)
        return false;

    job.value = Job.fromServer(result.data, info.value);
    if (job.value.state === JobState.Failed || job.value.state === JobState.Waiting)
        return 'no-refetch';

    return true;
}
</script>

<template>
    <h1>Inference job</h1>
    <p>
        The Inference job has generated an initial Schema Category. While in 'Waiting' status, you can adjust the schema. Once edits are complete, click 'Save and Finish' to set the job status to 'Finished.' This confirms your edits and unlocks the next step in the workflow.
    </p>
    <div
        v-if="job"
        class="d-flex flex-column"
    >
        <JobDisplay
            :job="job"
            :is-show-detail="true"
            @update-job="newJob => job = newJob"
        />
    </div>
    <ResourceLoader
        :loading-function="fetchJob"
        :refresh-period="2000"
    />
    <Teleport to="#app-left-bar-content">
        <button
            class="mt-4 order-2"
            :disabled="job?.state !== JobState.Finished"
            @click="emit('continue')"
        >
            Continue
        </button>
    </Teleport>
</template>
