<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { RouterLink } from 'vue-router';
import JobDisplay from '../components/job/JobDisplay.vue';
import NewJob from '../components/job/NewJob.vue';

export default defineComponent({
    components: {
    ResourceNotFound,
    ResourceLoading,
    RouterLink,
    JobDisplay,
    NewJob
},
    props: {

    },
    data() {
        return {
            jobs: null as Job[] | null,
            fetched: false
        };
    },
    async mounted() {
        const result = await GET<Job[]>('/jobs');
        if (result.status)
            this.jobs = [ ...result.data ];

        this.fetched = true;
    }
});
</script>

<template>
    <div>
        <h1>This is a jobs page</h1>
        <div class="jobs" v-if="jobs">
            <div v-for="job in jobs">
                <JobDisplay :job="job" />
            </div>
            <NewJob />
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
