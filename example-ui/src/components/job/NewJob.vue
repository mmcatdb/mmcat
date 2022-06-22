<script lang="ts">
import { defineComponent } from 'vue';
import { type Job, JOB_TYPES } from '@/types/job';
import { GET, POST } from '@/utils/backendAPI';
import type { MappingFromServer } from '@/types/mapping';

export default defineComponent({
    components: {

    },
    emits: [ 'newJob' ],
    data() {
        return {
            mappings: null as MappingFromServer[] | null,
            fetched: false,
            mappingId: null as number | null,
            jobName: '',
            jobType: '',
            availableJobTypes: JOB_TYPES,
            createJobDisabled: false
        };
    },
    async mounted() {
        const result = await GET<MappingFromServer[]>('/mappings');
        if (result.status) {
            this.mappings = [ ...result.data ];
            this.mappingId = this.mappings[0]?.id;
        }

        this.fetched = true;
    },
    methods: {
        async createJob() {
            this.createJobDisabled = true;

            const result = await POST<Job>('/jobs', { mappingId: this.mappingId, name: this.jobName, type: this.jobType });
            if (result.status)
                this.$emit('newJob', result.data);

            this.createJobDisabled = false;
        }
    }
});
</script>

<template>
    <div class="newJob">
        <h2>Create a new job</h2>
        <table>
            <tr>
                <td class="label">
                    Name:
                </td>
                <td class="value">
                    <input v-model="jobName" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    <select v-model="jobType">
                        <option
                            v-for="availableType in availableJobTypes"
                            :key="availableType.value"
                            :value="availableType.value"
                        >
                            {{ availableType.label }}
                        </option>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="label">
                    Mapping:
                </td>
                <td class="value">
                    <select v-model="mappingId">
                        <option
                            v-for="(mapping, index) in mappings"
                            :key="index"
                            :value="mapping.id"
                        >
                            {{ mapping.jsonValue }}
                        </option>
                    </select>
                </td>
            </tr>
            <tr>
                &nbsp;<!-- To make the NewJob tile look the same as the JobDisplay tile. -->
            </tr>
        </table>
        <div class="button-row">
            <button
                :disabled="createJobDisabled"
                @click="createJob"
            >
                Create job
            </button>
        </div>
    </div>
</template>

<style scoped>
.newJob {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 284px;
}
</style>
