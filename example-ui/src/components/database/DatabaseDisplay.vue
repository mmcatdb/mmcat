<script lang="ts">
import type { Database } from '@/types/database';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        database: {
            type: Object as () => Database,
            required: true
        }
    },
    emits: [ 'edit' ],
    data() {
        return {

        };
    },
    methods: {
        edit() {
            this.$emit('edit');
        }
    }
});
</script>

<template>
    <div class="database-display">
        <RouterLink :to="{ name: 'database', params: { id: database.id } }">
            <h2>{{ database.label }}</h2>
        </RouterLink>
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
}
</style>
