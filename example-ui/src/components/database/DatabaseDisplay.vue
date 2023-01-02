<script setup lang="ts">
import type { Database } from '@/types/database';
import CleverRouterLink from '@/components/CleverRouterLink.vue';

interface DatabaseDisplayProps {
    database: Database;
}

defineProps<DatabaseDisplayProps>();

const emit = defineEmits([ 'edit' ]);

function edit() {
    emit('edit');
}
</script>

<template>
    <div class="database-display">
        <CleverRouterLink :to="{ name: 'database', params: { id: database.id } }">
            <h2>{{ database.label }}</h2>
        </CleverRouterLink>
        <table>
            <tr>
                <td class="label">
                    Id:
                </td>
                <td class="value">
                    {{ database.id }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    {{ database.type }}
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                @click="edit"
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
