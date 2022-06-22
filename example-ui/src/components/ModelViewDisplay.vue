<script lang="ts">
import { defineComponent } from 'vue';
import type { ModelView } from '@/types/model';

export default defineComponent({
    props: {
        model: {
            type: Object as () => ModelView,
            required: true
        }
    },
    data() {
        return {
            textArea: null as HTMLTextAreaElement | null
        };
    },
    methods: {

    }
});
</script>

<template>
    <div class="model-display">
        <RouterLink :to="{ name: 'model', params: { jobId: model.jobId } }">
            <h2>{{ model.jobName }}</h2>
        </RouterLink>
        <table>
            <tr>
                <td class="label">
                    Job id:
                </td>
                <td class="value">
                    {{ model.jobId }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Job name:
                </td>
                <td class="value">
                    {{ model.jobName }}
                </td>
            </tr>
        </table>
    </div>
</template>

<style scoped>
.model-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 244px;
}
</style>
