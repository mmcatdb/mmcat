<script setup lang="ts">
import type { Mapping } from '@/types/mapping';
import ParentPropertyDisplay from './display/ParentPropertyDisplay.vue';
import Divider from '@/components/layout/Divider.vue';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureIdDisplay from '../category/SignatureIdDisplay.vue';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';
import VersionDisplay from '../VersionDisplay.vue';

type MappingDisplayProps = {
    mapping: Mapping;
};

defineProps<MappingDisplayProps>();

const category = useSchemaCategory();
</script>

<template>
    <div class="mapping-display">
        <CleverRouterLink :to="{ name: 'mapping', params: { id: mapping.id } }">
            <h2>{{ mapping.kindName }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ mapping.id }}
            </ValueRow>
            <ValueRow label="Version:">
                <VersionDisplay :version="mapping.version" />
            </ValueRow>
            <ValueRow
                v-if="mapping.categoryVersion !== category.version"
                label="Category Version:"
            >
                <VersionDisplay
                    :version="mapping.categoryVersion"
                    :error="true"
                />
            </ValueRow>
            <ValueRow label="Root object:">
                {{ mapping.rootObject.label }}
            </ValueRow>
            <ValueRow label="Primary key:">
                <SignatureIdDisplay
                    :signature-id="mapping.primaryKey"
                />
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
