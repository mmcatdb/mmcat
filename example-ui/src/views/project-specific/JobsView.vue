<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { Job } from '@/types/job';
import API from '@/utils/api';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';
import NewJob from '@/components/job/NewJob.vue';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';

const jobs = ref<Job[]>();
const fetched = ref(false);
const continueFetching = ref(true);

onMounted(async () => {
    await fetchNew();

    fetched.value = true;
});

onUnmounted(() => {
    // The reason for this variable is that we want to stop additional fetches while the fetchNew is in the middle of the fetching.
    // So just clearing the timeout isn't enough.
    continueFetching.value = false;
});

function addNewJob(job: Job) {
    jobs.value?.push(job);
}

function deleteJob(id: number) {
    jobs.value = jobs.value?.filter(job => job.id !== id) ?? [];
}

const schemaCategoryId = useSchemaCategory();

async function fetchNew() {
    const result = await API.jobs.getAllJobsInCategory({ categoryId: schemaCategoryId });
    if (result.status)
        jobs.value = result.data.map(Job.fromServer);

    if (continueFetching.value)
        setTimeout(fetchNew, 1000);
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
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.jobs {
    display: flex;
    flex-wrap: wrap;
}
</style>
