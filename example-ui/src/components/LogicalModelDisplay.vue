<script lang="ts">
import { defineComponent } from 'vue';
import type { LogicalModelInfo } from '@/types/logicalModel';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import type { DatabaseInfo } from '@/types/database';

export default defineComponent({
    components: {
        CleverRouterLink
    },
    props: {
        logicalModel: {
            type: Object as () => LogicalModelInfo,
            required: true
        },
        database: {
            type: Object as () => DatabaseInfo,
            required: false,
            default: undefined
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
    <div class="logical-model-display">
        <CleverRouterLink :to="{ name: 'logicalModel', params: { id: logicalModel.id } }">
            <h2>{{ logicalModel.label }}</h2>
        </CleverRouterLink>
        <table>
            <tr>
                <td class="label">
                    Id:
                </td>
                <td class="value">
                    {{ logicalModel.id }}
                </td>
            </tr>
            <tr v-if="database">
                <td class="label">
                    Database:
                </td>
                <td class="value">
                    <RouterLink :to="{ name: 'databases', params: { id: database.id } }">
                        {{ database.label }}
                    </RouterLink>
                </td>
            </tr>
        </table>
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
