<script setup lang="ts">
import type { Datasource } from '@/types/datasource';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import IriDisplay from '../common/IriDisplay.vue';

defineProps<{
    datasource: Datasource;
    scondaryButton?: string;
}>();

const emit = defineEmits([ 'edit', 'onSecondaryClick' ]);
</script>

<template>
    <div class="datasource-display d-flex flex-column">
        <FixedRouterLink :to="{ name: 'datasource', params: { id: datasource.id } }">
            <h2>{{ datasource.label }}</h2>
        </FixedRouterLink>
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
        <div class="flex-grow-1" />
        <div class="button-row">
            <button
                @click="emit('edit')"
            >
                Edit
            </button>
            <button
                v-if="scondaryButton"
                @click="emit('onSecondaryClick')"
            >
                {{ scondaryButton }}
            </button>
        </div>
    </div>
</template>

<style scoped>
.datasource-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    min-width: 204px;
}
</style>
