<script lang="ts">
import type { Job } from '@/types/job';
import { DELETE, POST } from '@/utils/backendAPI';
import { defineComponent } from 'vue';
import { RouterLink } from 'vue-router';

export default defineComponent({
    props: {
        job: {
            type: Object as () => Job,
            required: true
        }
    },
    emits: [ 'deleteJob' ],
    data() {
        return {
            startJobDisabled: false,
            deleteJobDisabled: false,
            restartJobDisabled: false
        };
    },
    methods: {
        async startJob() {
            this.startJobDisabled = true;
            console.log('Starting job:', this.job);

            const result = await POST<Job>(`/jobs/${this.job.id}/start`);
            if (result.status)
                this.job.status = result.data.status;

            console.log({ result });

            this.startJobDisabled = false;
        },
        async deleteJob() {
            this.deleteJobDisabled = true;
            console.log('Deleting job:', this.job);

            const result = await DELETE<Job>(`/jobs/${this.job.id}`);
            if (result.status)
                this.$emit('deleteJob');

            console.log({ result });

            this.deleteJobDisabled = false;
        },
        async restartJob() {
            this.restartJobDisabled = true;
            console.log('Restarting job:', this.job);

            const result = await POST<Job>(`/jobs/${this.job.id}/start`);
            if (result.status)
                this.job.status = result.data.status;

            console.log({ result });

            this.restartJobDisabled = false;
        }
    }
});
</script>

<template>
    <div class="job-display">
        <RouterLink :to="{ name: 'job', params: { id: job.id } }">
            <h2>{{ job.name }}</h2>
        </RouterLink>
        <table>
            <tr>
                <td class="label">
                    Id:
                </td>
                <td class="value">
                    {{ job.id }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Mapping id:
                </td>
                <td class="value">
                    {{ job.mappingId }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    {{ job.type }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Status:
                </td>
                <td class="value">
                    {{ job.status }}
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                v-if="job.status === 'Ready'"
                :disabled="startJobDisabled"
                class="success"
                @click="startJob"
            >
                Start job
            </button>
            <button
                v-if="job.status === 'Finished' || job.status === 'Canceled'"
                :disabled="deleteJobDisabled"
                class="error"
                @click="deleteJob"
            >
                Delete job
            </button>
            <button
                v-if="job.status === 'Finished' || job.status === 'Canceled'"
                :disabled="restartJobDisabled"
                class="warning"
                @click="restartJob"
            >
                Restart job
            </button>
        </div>
    </div>
</template>

<style scoped>
.job-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
}
</style>
