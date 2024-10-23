<script setup lang="ts">
import type { LogicalModelInfo } from '@/types/logicalModel';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { useRoute } from 'vue-router';

type LogicalModelDisplayProps = {
    logicalModel: LogicalModelInfo;
};

defineProps<LogicalModelDisplayProps>();
</script>

<template>
    <div class="logical-model-display">
        <FixedRouterLink :to="{ name: 'logicalModel', params: { id: logicalModel.id } }">
            <h2>{{ logicalModel.label }}</h2>
        </FixedRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ logicalModel.id }}
            </ValueRow>
            <ValueRow
                label="Datasource:"
            >
                <FixedRouterLink :to="{ name: 'datasource', params: { id: logicalModel.datasource.id } }">
                    {{ logicalModel.datasource.label }}
                </FixedRouterLink>
            </ValueRow>
        </ValueContainer>
    </div>
</template>

<style scoped>
.logical-model-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 244px;
}
</style>
