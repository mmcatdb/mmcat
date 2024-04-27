<script setup lang="ts">
import {  } from 'vue';
import type { LogicalModelInfo } from '@/types/logicalModel';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { useRoute } from 'vue-router';

type LogicalModelDisplayProps = {
    logicalModel: LogicalModelInfo;
};

defineProps<LogicalModelDisplayProps>();

const route = useRoute();
</script>

<template>
    <div class="logical-model-display">
        <CleverRouterLink :to="{ name: 'logicalModel', params: { id: logicalModel.id } }">
            <h2>{{ logicalModel.label }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ logicalModel.id }}
            </ValueRow>
            <ValueRow
                label="Datasource:"
            >
                <RouterLink :to="{ name: 'datasource', params: { id: logicalModel.datasource.id }, query: { categoryId: route.params.categoryId } }">
                    {{ logicalModel.datasource.label }}
                </RouterLink>
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
