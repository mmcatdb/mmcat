<script setup lang="ts">
import { ref } from 'vue';
import { Job } from '@/types/job';
import API from '@/utils/api';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { ActionType } from '@/types/action';

const info = useSchemaCategoryInfo();

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

async function fetchJobs() {
    const result = await API.jobs.getAllJobsInCategory({ categoryId: info.value.id });
    if (!result.status)
        return false;

    jobs.value = result.data
        .map(job => Job.fromServer(job, info.value))
        .filter(job => job.payload.type === ActionType.ModelToCategory || job.payload.type === ActionType.CategoryToModel)
        .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());

    return true;
}
</script>

<template>
    <div>
        <h1>Results</h1>
        <div
            v-if="jobs"
            class="d-flex flex-column gap-3"
        >
            <div
                v-for="job in jobs"
                :key="job.id"
            >
                <JobDisplay
                    :job="job"
                    :is-show-detail="true"
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
