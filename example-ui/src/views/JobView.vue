<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET, POST } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading
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
        async startJob() {
            this.startJobDisabled = true;
            console.log('Starting job:', this.job);

            const result = await POST<Job>(`/jobs/${this.job!.id}/start`);
            if (result.status)
                this.job = result.data;

            console.log({ result });

            this.startJobDisabled = false;
        }
    }
});
</script>

<template>
    <h1>This is a job</h1>
    <template v-if="job">
        <p>
            {{ job.id }}<br>
            {{ job.status }}
        </p>
        <button
            :disabled="startJobDisabled"
            @click="startJob"
        >
            Start job
        </button>
    </template>
    <ResourceNotFound v-else-if="jobFetched" />
    <ResourceLoading v-else />
</template>

<style>

</style>
