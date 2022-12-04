<script lang="ts">
import { defineComponent } from 'vue';
import { type Job, JOB_TYPES, type JobInit, JobType } from '@/types/job';
import { GET, POST } from '@/utils/backendAPI';
import { LogicalModel, type LogicalModelFromServer } from '@/types/logicalModel';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';

export default defineComponent({
    components: {

    },
    emits: [ 'newJob' ],
    data() {
        return {
            logicalModels: null as LogicalModel[] | null,
            fetched: false,
            logicalModelId: null as number | null,
            jobName: '',
            jobType: undefined as JobType | undefined,
            availableJobTypes: JOB_TYPES,
            fetching: false
        };
    },
    async mounted() {
        const result = await GET<LogicalModelFromServer[]>(`/schema-categories/${getSchemaCategoryId()}/logical-models`);
        if (result.status)
            this.logicalModels = result.data.map(LogicalModel.fromServer);

        this.fetched = true;
    },
    methods: {
        async createJob() {
            if (!this.logicalModelId || !this.jobType)
                return;

            this.fetching = true;

            const result = await POST<Job, JobInit>('/jobs', {
                logicalModelId: this.logicalModelId,
                label: this.jobName,
                type: this.jobType
            });
            if (result.status)
                this.$emit('newJob', result.data);

            this.fetching = false;
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
                    Label:
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
                    Logical model:
                </td>
                <td class="value">
                    <select v-model="logicalModelId">
                        <option
                            v-for="logicalModel in logicalModels"
                            :key="logicalModel.id"
                            :value="logicalModel.id"
                        >
                            {{ logicalModel.label }}
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
                :disabled="(fetching || !jobName || !jobType || !logicalModelId)"
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
