<script lang="ts">
import { defineComponent } from 'vue';
import type { LogicalModelInfo } from '@/types/logicalModel';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import type { DatabaseInfo } from '@/types/database';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

export default defineComponent({
    components: {
        CleverRouterLink,
        ValueContainer,
        ValueRow
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
        <ValueContainer>
            <ValueRow label="Id:">
                {{ logicalModel.id }}
            </ValueRow>
            <ValueRow
                v-if="database"
                label="Database:"
            >
                <RouterLink :to="{ name: 'database', params: { id: database.id } }">
                    <!--TODO-->
                    {{ database.label }}
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
