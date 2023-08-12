<script setup lang="ts">
import { ref } from 'vue';
import { Job } from '@/types/job';
import API from '@/utils/api';

import ResourceLoader from '@/components/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';
import NewJob from '@/components/job/NewJob.vue';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';

const jobs = ref<Job[]>();

function addNewJob(job: Job) {
    jobs.value?.push(job);
}

function deleteJob(id: Id) {
    jobs.value = jobs.value?.filter(job => job.id !== id) ?? [];
}

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
            <NewJob
                :key="jobs.length"
                @new-job="addNewJob"
            />
            <div
                v-for="job in jobs"
                :key="job.id"
            >
                <JobDisplay
                    :job="job"
                    @delete-job="() => deleteJob(job.id)"
                    @update-job="updateJob"
                />
            </div>
        </div>
        <ResourceLoader
            :loading-function="fetchJobs"
            :refresh-period="1000000"
        />
    </div>
</template>

<style scoped>
.jobs {
    display: flex;
    flex-wrap: wrap;
}
</style>
