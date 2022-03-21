<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET, POST } from '@/utils/backendAPI';
import type { Mapping } from '@/types/mapping';

export default defineComponent({
    components: {

    },
    props: {},
    data() {
        return {
            mappings: null as Mapping[] | null,
            fetched: false,
            mappingId: null as number | null,
            createJobDisabled: false
        };
    },
    async mounted() {
        const result = await GET<Mapping[]>('/mappings');
        if (result.status) {
            this.mappings = [ ...result.data ];
            this.mappingId = this.mappings[0]?.id;
        }

        this.fetched = true;
    },
    emits: [ 'newJob' ],
    methods: {
        async createJob() {
            this.createJobDisabled = true;
            console.log('New job is being created.');

            const result = await POST<Job>('/jobs', { mappingId: this.mappingId });
            console.log(result);
            if (result.status)
                this.$emit('newJob', result.data);

            this.createJobDisabled = false;
        }
    }
});
</script>

<template>
<div class="newJob">
    <h2>This is going to be a new job</h2>
    <label>Select mapping:</label><br>
    <select v-model="mappingId">
        <option v-for="mapping in mappings" :value="mapping.id">{{ mapping.jsonValue }}</option>
    </select>
        <button
        :disabled="createJobDisabled"
        @click="createJob"
    >
        Create job
    </button>
</div>
</template>

<style scoped>
.newJob {
    padding: 8px;
    border: 1px solid hsla(160, 100%, 37%, 1);
    margin-right: 16px;
}
</style>
