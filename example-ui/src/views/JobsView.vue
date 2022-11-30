<script lang="ts">
import { defineComponent } from 'vue';
import { Job, type JobFromServer } from '@/types/job';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import JobDisplay from '../components/job/JobDisplay.vue';
import NewJob from '../components/job/NewJob.vue';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';

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
            const result = await GET<JobFromServer[]>(`/schema-categories/${getSchemaCategoryId()}/jobs`);
            if (result.status)
                this.jobs = result.data.map(Job.fromServer);

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
            <NewJob @new-job="addNewJob" />
            <div
                v-for="(job, index) in jobs"
                :key="index"
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
