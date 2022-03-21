<script lang="ts">
import type { Job } from '@/types/job';
import { POST } from '@/utils/backendAPI';
import { defineComponent } from 'vue';
import { RouterLink } from 'vue-router';

export default defineComponent({
    props: {
        job: {
            type: Object as () => Job,
            required: true
        }
    },
    data() {
        return {
            startJobDisabled: false
        };
    },
    methods: {
        async startJob() {
            this.startJobDisabled = true;
            console.log('Starting job:', this.job);

            const result = await POST<Job>(`/jobs/${this.job!.id}/start`);
            if (result.status)
                this.job.status = result.data.status;

            console.log({ result });

            this.startJobDisabled = false;
        }
    }
});
</script>

<template>
<div class="jobDisplay">
    <RouterLink :to="{ name: 'job', params: { id: job.id } }">
        <h2>Job name</h2>
    </RouterLink>
    <p>
        Id: {{ job.id }}<br>
        Mapping id: {{ job.mappingId }}<br>
        Status: {{ job.status }}
    </p>
    <button
        v-if="job.status === 'Ready'"
        :disabled="startJobDisabled"
        @click="startJob"
    >
        Start job
    </button>
</div>
</template>

<style scoped>
.jobDisplay {
    padding: 8px;
    border: 1px solid hsla(160, 100%, 37%, 1);
    margin-right: 16px;
}
</style>
