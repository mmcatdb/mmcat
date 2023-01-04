<script lang="ts">
import { defineComponent } from 'vue';
import type { Mapping } from '@/types/mapping';
import ParentPropertyDisplay from './display/ParentPropertyDisplay.vue';
import Divider from '@/components/layout/Divider.vue';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

export default defineComponent({
    components: {
        ParentPropertyDisplay,
        Divider,
        CleverRouterLink,
        ValueContainer,
        ValueRow
    },
    props: {
        mapping: {
            type: Object as () => Mapping,
            required: true
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
    <div class="mapping-display">
        <CleverRouterLink :to="{ name: 'mapping', params: { id: mapping.id } }">
            <h2>{{ mapping.label }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ mapping.id }}
            </ValueRow>
            <ValueRow label="Root object id:">
                {{ mapping.rootObjectId }}
            </ValueRow>
            <!--
            <ValueRow label="Logical model:">
                {{ mapping.logicalModel.label }}
            </ValueRow>
            <ValueRow label="Database:">
                {{ mapping.logicalModel.database.label }}
            </ValueRow>
            -->
        </ValueContainer>
        <Divider />
        <ParentPropertyDisplay
            :property="mapping.accessPath"
            :disable-additions="true"
        />
    </div>
</template>

<style scoped>
.mapping-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 244px;
}
</style>
