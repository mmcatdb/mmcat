<script lang="ts">
import { type Job, Status } from '@/types/job';
import { DELETE, POST } from '@/utils/backendAPI';
import { defineComponent } from 'vue';
import CleverRouterLink from '@/components/CleverRouterLink.vue';

export default defineComponent({
    components: {
        CleverRouterLink
    },
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
            restartJobDisabled: false,
            Status
        };
    },
    computed: {
        jobStatusClass(): string {
            switch (this.job.status) {
            case Status.Canceled:
                return 'text-error';
            case Status.Finished:
                return 'text-success';
            default:
                return '';
            }
        }
    },
    methods: {
        async startJob() {
            this.startJobDisabled = true;

            const result = await POST<Job>(`/jobs/${this.job.id}/start`);
            if (result.status)
                this.job.setStatus(result.data.status);

            this.startJobDisabled = false;
        },
        async deleteJob() {
            this.deleteJobDisabled = true;

            const result = await DELETE<Job>(`/jobs/${this.job.id}`);
            if (result.status)
                this.$emit('deleteJob');

            this.deleteJobDisabled = false;
        },
        async restartJob() {
            this.restartJobDisabled = true;

            const result = await POST<Job>(`/jobs/${this.job.id}/start`);
            if (result.status)
                this.job.setStatus(result.data.status);

            this.restartJobDisabled = false;
        }
    }
});
</script>

<template>
    <div class="job-display">
        <CleverRouterLink :to="{ name: 'job', params: { id: job.id } }">
            <h2>{{ job.label }}</h2>
        </CleverRouterLink>
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
                <td
                    :class="jobStatusClass"
                    class="value"
                >
                    {{ job.status }}
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                v-if="job.status === Status.Ready"
                :disabled="startJobDisabled"
                class="success"
                @click="startJob"
            >
                Start
            </button>
            <button
                v-if="job.status !== Status.Running"
                :disabled="deleteJobDisabled"
                class="error"
                @click="deleteJob"
            >
                Delete
            </button>
            <button
                v-if="job.status === Status.Finished || job.status === Status.Canceled"
                :disabled="restartJobDisabled"
                class="warning"
                @click="restartJob"
            >
                Restart
            </button>
        </div>
    </div>
</template>

<style scoped>
.job-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 284px;
}
</style>
