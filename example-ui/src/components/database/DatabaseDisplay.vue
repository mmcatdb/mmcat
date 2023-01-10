<script setup lang="ts">
import type { Database } from '@/types/database';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import type { Id } from '@/types/id';

type DatabaseDisplayProps = {
    database: Database;
    categoryId?: Id;
}

defineProps<DatabaseDisplayProps>();

const emit = defineEmits([ 'edit' ]);
</script>

<template>
    <div class="database-display">
        <CleverRouterLink :to="{ name: 'database', params: { id: database.id }, query: { categoryId } }">
            <h2>{{ database.label }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ database.id }}
            </ValueRow>
            <ValueRow label="Type:">
                {{ database.type }}
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
.database-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 204px;
}
</style>
