<script setup lang="ts">
import { ref } from 'vue';
import { Job } from '@/types/job';
import API from '@/utils/api';

import ResourceLoader from '@/components/common/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';
import { useRoute } from 'vue-router';

const job = ref<Job>();

const route = useRoute();

async function fetchJob() {
    const result = await API.jobs.getJob({ id: route.params.id });
    if (!result.status)
        return false;

    job.value = Job.fromServer(result.data);
    return true;
}
</script>

<template>
    <h1>Job</h1>
    <div
        v-if="job"
        class="job"
    >
        <JobDisplay
            :job="job"
            @update-job="newJob => job = newJob"
        />
    </div>
    <ResourceLoader :loading-function="fetchJob" />
</template>

<style scoped>
.job {
    display: flex;
}
</style>
