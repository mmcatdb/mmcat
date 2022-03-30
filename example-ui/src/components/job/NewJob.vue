<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { GET, POST } from '@/utils/backendAPI';
import type { Mapping } from '@/types/mapping';
import { RootProperty } from '@/types/accessPath';
//import { accessPathFromJSON } from '@/types/accessPath';

export default defineComponent({
    components: {

    },
    props: {},
    data() {
        return {
            mappings: null as Mapping[] | null,
            fetched: false,
            mappingId: null as number | null,
            jobName: '',
            createJobDisabled: false
        };
    },
    async mounted() {
        const result = await GET<Mapping[]>('/mappings');
        if (result.status) {
            this.mappings = [ ...result.data ];
            this.mappingId = this.mappings[0]?.id;

            const jsonString = this.mappings[0].mappingJsonValue;
            console.log(jsonString);
            const jsonObject = JSON.parse(jsonString);
            const path = RootProperty.fromJSON(jsonObject.accessPath);
            console.log({ path });
            console.log(path.toString());
        }

        this.fetched = true;
    },
    emits: [ 'newJob' ],
    methods: {
        async createJob() {
            this.createJobDisabled = true;
            console.log('New job is being created.');

            const result = await POST<Job>('/jobs', { mappingId: this.mappingId, name: this.jobName });
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
        <label>Name:</label>
        <br>
        <input v-model="jobName" />
        <br>
        <label>Select mapping:</label>
        <br>
        <select v-model="mappingId">
            <option v-for="mapping in mappings" :value="mapping.id">{{ mapping.jsonValue }}</option>
        </select>
        <br>
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
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    /*
    display: flex;
    flex-direction: column;
    */
}
</style>
