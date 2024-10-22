<script setup lang="ts">
import { ref } from 'vue';
import { Job, JobState } from '@/types/job';
import API from '@/utils/api';
import { useSchemaCategoryInfo } from '@/utils/injects';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';
import { useRoute } from 'vue-router';

const job = ref<Job>();
const info = useSchemaCategoryInfo();

const route = useRoute();

async function fetchJob() {
    const result = await API.jobs.getJob({ id: route.params.id });
    if (!result.status)
        return false;

    job.value = Job.fromServer(result.data, info.value);
    if (job.value.state === JobState.Failed || job.value.state === JobState.Waiting)
        return 'no-refetch';

    return true;
}
</script>

<template>
    <h1>Job</h1>
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
</template>
