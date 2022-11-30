<script lang="ts">
import { defineComponent } from 'vue';
import type { ModelView } from '@/types/model';
import CleverRouterLink from '@/components/CleverRouterLink.vue';

export default defineComponent({
    components: {
        CleverRouterLink
    },
    props: {
        model: {
            type: Object as () => ModelView,
            required: true
        }
    },
    data() {
        return {

        };
    },
    methods: {

    }
});
</script>

<template>
    <div class="model-display">
        <CleverRouterLink :to="{ name: 'model', params: { jobId: model.jobId } }">
            <h2>{{ model.jobLabel }}</h2>
        </CleverRouterLink>
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
                    Job label:
                </td>
                <td class="value">
                    {{ model.jobLabel }}
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
