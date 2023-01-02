<script lang="ts">
import { defineComponent } from 'vue';
import { Job } from '@/types/job';
import API from '@/utils/api';

import ResourceLoader from '@/components/ResourceLoader.vue';
import JobDisplay from '@/components/job/JobDisplay.vue';

export default defineComponent({
    components: {
        ResourceLoader,
        JobDisplay
    },
    props: {},
    data() {
        return {
            job: null as Job | null,
            startJobDisabled: false
        };
    },
    methods: {
        deleteJob(): void {
            this.$router.push({ name: 'jobs' });
        },
        async fetchJob() {
            const result = await API.jobs.getJob({ id: this.$route.params.id });
            if (!result.status)
                return false;

            this.job = Job.fromServer(result.data);
            return true;
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
    <ResourceLoader :loading-function="fetchJob" />
</template>

<style scoped>
.job {
    display: flex;
}
</style>
