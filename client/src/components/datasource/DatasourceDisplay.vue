<script setup lang="ts">
import type { Datasource } from '@/types/datasource';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import type { Id } from '@/types/id';
import IriDisplay from '../common/IriDisplay.vue';

type DatasourceDisplayProps = {
    datasource: Datasource;
    categoryId?: Id;
};

defineProps<DatasourceDisplayProps>();

const emit = defineEmits([ 'edit' ]);
</script>

<template>
    <div class="datasource-display">
        <CleverRouterLink :to="{ name: 'datasource', params: { id: datasource.id }, query: { categoryId } }">
            <h2>{{ datasource.label }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ datasource.id }}
            </ValueRow>
            <ValueRow label="Type:">
                {{ datasource.type }}
            </ValueRow>
            <ValueRow
                label="Url:"
                :class="{ 'opacity-0': !datasource.settings.url }"
            >
                <IriDisplay
                    :iri="datasource.settings.url"
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
.datasource-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 204px;
}
</style>
