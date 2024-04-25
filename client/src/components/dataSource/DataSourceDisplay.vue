<script setup lang="ts">
import type { DataSource } from '@/types/dataSource';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import type { Id } from '@/types/id';
import IriDisplay from '../common/IriDisplay.vue';

type DataSourceDisplayProps = {
    dataSource: DataSource;
    categoryId?: Id;
};

defineProps<DataSourceDisplayProps>();

const emit = defineEmits([ 'edit' ]);
</script>

<template>
    <div class="data-source-display">
        <CleverRouterLink :to="{ name: 'dataSource', params: { id: dataSource.id }, query: { categoryId } }">
            <h2>{{ dataSource.label }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ dataSource.id }}
            </ValueRow>
            <ValueRow label="Type:">
                {{ dataSource.type }}
            </ValueRow>
            <ValueRow
                label="Url:"
                :class="{ 'opacity-0': !dataSource.settings.url }"
            >
                <IriDisplay
                    :iri="dataSource.settings.url"
                    :max-chars="36"
                    clickable
                />
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                @click="emit('edit')"
            >
                Edit
            </button>
        </div>
    </div>
</template>

<style scoped>
.data-source-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 204px;
}
</style>
