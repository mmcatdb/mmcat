<script lang="ts">
import { defineComponent } from 'vue';
import { Job, type JobFromServer } from '@/types/job';
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
        const result = await GET<JobFromServer>(`/jobs/${this.$route.params.id}`);
        if (result.status)
            this.job = Job.fromServer(result.data);

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
    <div
        v-if="job"
        class="job"
    >
        <JobDisplay
            :job="job"
            @delete-job="deleteJob"
        />
    </div>
    <ResourceNotFound v-else-if="jobFetched" />
    <ResourceLoading v-else />
</template>

<style scoped>
.job {
    display: flex;
}
</style>
