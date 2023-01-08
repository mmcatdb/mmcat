<script setup lang="ts">
import { ref } from 'vue';
import { Job } from '@/types/job';
import API from '@/utils/api';

import ResourceLoader from '@/components/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';
import NewJob from '@/components/job/NewJob.vue';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';
import type { Id } from '@/types/id';

const jobs = ref<Job[]>();

function addNewJob(job: Job) {
    jobs.value?.push(job);
}

function deleteJob(id: Id) {
    jobs.value = jobs.value?.filter(job => job.id !== id) ?? [];
}

const categoryId = useSchemaCategory();

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
            <NewJob @new-job="addNewJob" />
            <div
                v-for="job in jobs"
                :key="job.id"
            >
                <JobDisplay
                    :job="job"
                    @delete-job="() => deleteJob(job.id)"
                />
            </div>
        </div>
        <ResourceLoader
            :loading-function="fetchJobs"
            :refresh-period="1000"
        />
    </div>
</template>

<style scoped>
.jobs {
    display: flex;
    flex-wrap: wrap;
}
</style>
