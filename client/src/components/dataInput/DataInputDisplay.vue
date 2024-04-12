<script setup lang="ts">
import type { DataInput } from '@/types/dataInput';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import type { Id } from '@/types/id';

type DataInputDisplayProps = {
    dataInput: DataInput;
    categoryId?: Id;
};

defineProps<DataInputDisplayProps>();

const emit = defineEmits([ 'edit' ]);
</script>

<template>
    <div class="data-input-display">
        <CleverRouterLink :to="{ name: 'dataInput', params: { id: dataInput.id }, query: { categoryId } }">
            <h2>{{ dataInput.label }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ dataInput.id }}
            </ValueRow>
            <ValueRow label="Type:">
                {{ dataInput.type }}
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
.data-input-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 204px;
}
</style>
