<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import JobDisplay from '../components/job/JobDisplay.vue';
import NewJob from '../components/job/NewJob.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        JobDisplay,
        NewJob
    },
    data() {
        return {
            jobs: null as Job[] | null,
            fetched: false,
            continueFetching: true
        };
    },
    async mounted() {
        await this.fetchNew();

        this.fetched = true;
    },
    unmounted() {
        // The reason for this variable is that we want to stop additional fetches while the fetchNew is in the middle of the fetching.
        // So just clearing the timeout isn't enough.
        this.continueFetching = false;
    },
    methods: {
        addNewJob(job: Job) {
            this.jobs?.push(job);
        },
        deleteJob(id: number) {
            this.jobs = this.jobs?.filter(job => job.id !== id) ?? [];
        },
        async fetchNew() {
            const result = await GET<Job[]>('/jobs');
            if (result.status)
                this.jobs = result.data;

            if (this.continueFetching)
                setTimeout(this.fetchNew, 1000);
        }
    }
});
</script>

<template>
    <div>
        <h1>Jobs</h1>
        <div
            v-if="jobs"
            class="jobs"
        >
            <div
                v-for="(job, index) in jobs"
                :key="index"
            >
                <JobDisplay
                    :job="job"
                    @delete-job="() => deleteJob(job.id)"
                />
            </div>
            <NewJob @new-job="addNewJob" />
        </div>
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.jobs {
    display: flex;
}
</style>
