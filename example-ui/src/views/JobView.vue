<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET } from '@/utils/backendAPI';

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
            jobFetched: false
        };
    },
    async mounted() {
        const result = await GET<Job>(`/jobs/${this.$route.params.id}`);
        if (result.status)
            this.job = result.data;

        this.jobFetched = true;
    }
});
</script>

<template>
    <h1>This is a job</h1>
    <p v-if="job">
        {{ job.id }}<br>
        {{ job.value }}
    </p>
    <ResourceNotFound v-else-if="jobFetched" />
    <ResourceLoading v-else />
</template>

<style>

</style>
