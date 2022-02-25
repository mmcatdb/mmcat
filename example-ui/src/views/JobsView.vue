<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { RouterLink } from 'vue-router';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        RouterLink
    },
    props: {

    },
    data() {
        return {
            jobs: null as Job[] | null,
            jobsFetched: false
        };
    },
    async mounted() {
        const result = await GET<Job[]>('/jobs');
        if (result.status)
            this.jobs = [ ...result.data ];

        this.jobsFetched = true;
    }
});
</script>

<template>
    <div>
        <h1>This is a jobs page</h1>
        <template v-if="jobs">
            <p
                v-for="job in jobs"
                :key="job.id"
            >
                <RouterLink :to="{ name: 'job', params: { id: job.id } }">
                    {{ job.id }}<br>
                    {{ job.value }}
                </RouterLink>
            </p>
        </template>
        <ResourceNotFound v-else-if="jobsFetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style>

</style>
