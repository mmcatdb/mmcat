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
    props: {

    },
    data() {
        return {
            jobs: null as Job[] | null,
            fetched: false,
            timeoutId: null as number | null
        };
    },
    async mounted() {
        await this.fetchNew();

        this.fetched = true;
    },
    unmounted() {
        if (this.timeoutId !== null)
            clearTimeout(this.timeoutId);
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
                this.jobs = [ ...result.data ];

            if (this.timeoutId === null)
                this.timeoutId = setTimeout(this.fetchNew, 1000);
        }
    }
});
</script>

<template>
    <div>
        <h1>This is a jobs page</h1>
        <div class="jobs" v-if="jobs">
            <div v-for="(job, index) in jobs" :key="index">
                <JobDisplay @delete-job="() => deleteJob(job.id)" :job="job" />
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
