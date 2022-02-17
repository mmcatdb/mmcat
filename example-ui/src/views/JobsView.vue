<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET } from '@/utils/backendAPI';

export default defineComponent({
    props: {

    },
    data() {
        return {
            jobs: [] as Job[]
        };
    },
    async mounted() {
        const result = await GET<Job[]>('/jobs');
        if (result.status)
            result.data.forEach(job => this.jobs.push(job));
        else
            console.log(result.error);
    }
});
</script>

<template>
    <div>
        <h1>This is a jobs page</h1>
        <p
            v-for="job in jobs"
            :key="job.id"
        >
            {{ job.id }}<br>
            {{ job.value }}
        </p>
    </div>
</template>

<style>
@media (min-width: 1024px) {

}
</style>
