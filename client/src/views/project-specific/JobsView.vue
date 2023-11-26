<script setup lang="ts">
import { ref } from 'vue';
import { Job } from '@/types/job';
import API from '@/utils/api';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';
import { useSchemaCategoryId } from '@/utils/injects';

const jobs = ref<Job[]>();

function updateJob(job: Job) {
    if (!jobs.value)
        return;

    const index = jobs.value.findIndex(j => j.id === job.id);
    if (index === -1)
        return;

    const newJobs = [ ...jobs.value ];
    newJobs[index] = job;
    jobs.value = newJobs;
}

const categoryId = useSchemaCategoryId();

async function fetchJobs() {
    const result = await API.jobs.getAllJobsInCategory({ categoryId });
    if (!result.status)
        return false;

    jobs.value = result.data.map(Job.fromServer);
    return true;
}
</script>

<template>
    <div>
        <h1>Jobs</h1>
        <div
            v-if="jobs"
            class="jobs"
        >
            <div
                v-for="job in jobs"
                :key="job.id"
            >
                <JobDisplay
                    :job="job"
                    @update-job="updateJob"
                />
            </div>
        </div>
        <ResourceLoader
            :loading-function="fetchJobs"
            :refresh-period="2000"
        />
    </div>
</template>

<style scoped>
.jobs {
    display: flex;
    flex-wrap: wrap;
}
</style>
