<script lang="ts">
import { defineComponent } from 'vue';
import type { ModelView } from '@/types/model';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

export default defineComponent({
    components: {
        CleverRouterLink,
        ValueContainer,
        ValueRow
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
        <ValueContainer>
            <ValueRow label="Job id:">
                {{ model.jobId }}
            </ValueRow>
            <ValueRow label="Job label:">
                {{ model.jobLabel }}
            </ValueRow>
        </ValueContainer>
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
