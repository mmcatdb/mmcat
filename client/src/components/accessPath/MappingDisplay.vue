<script setup lang="ts">
import type { Mapping } from '@/types/mapping';
import ParentPropertyDisplay from './display/ParentPropertyDisplay.vue';
import Divider from '@/components/layout/Divider.vue';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureIdDisplay from '../category/SignatureIdDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import VersionDisplay from '@/components/VersionDisplay.vue';

type MappingDisplayProps = {
    mapping: Mapping;
};

defineProps<MappingDisplayProps>();

const category = useSchemaCategoryInfo();
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
                <VersionDisplay :version-id="mapping.version" />
            </ValueRow>
            <ValueRow
                v-if="mapping.categoryVersionId !== category.versionId"
                label="Category Version:"
            >
                <VersionDisplay
                    :version-id="mapping.categoryVersionId"
                    :error="true"
                />
            </ValueRow>
            <ValueRow label="Root object key:">
                <!--
                    TODO - load whole schema category and display the object name that corresponds to this key
                -->
                {{ mapping.rootObjectKey }}
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
            <ValueRow label="Datasource:">
                {{ mapping.logicalModel.datasource.label }}
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
