<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        JobDisplay
    },
    props: {},
    data() {
        return {
            job: null as Job | null,
            jobFetched: false,
            startJobDisabled: false
        };
    },
    async mounted() {
        const result = await GET<Job>(`/jobs/${this.$route.params.id}`);
        if (result.status)
            this.job = result.data;

        this.jobFetched = true;
    },
    methods: {
        deleteJob(): void {
            this.$router.push({ name: 'jobs' });
        }
    }
});
</script>

<template>
    <h1>Job</h1>
    <div class="job" v-if="job">
        <JobDisplay @delete-job="deleteJob" :job="job" />
    </div>
    <ResourceNotFound v-else-if="jobFetched" />
    <ResourceLoading v-else />
</template>

<style scoped>
.job {
    display: flex;
}
</style>
