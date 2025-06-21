<script setup lang="ts">
import type { Mapping } from '@/types/mapping';
import ParentPropertyDisplay from './display/ParentPropertyDisplay.vue';
import Divider from '@/components/layout/Divider.vue';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureIdDisplay from '../category/SignatureIdDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import VersionDisplay from '@/components/VersionDisplay.vue';

defineProps<{
    mapping: Mapping;
    maxHeightPath?: boolean;
}>();

const category = useSchemaCategoryInfo();
</script>

<template>
    <div class="mapping-display">
        <FixedRouterLink :to="{ name: 'mapping', params: { id: mapping.id } }">
            <h2>{{ mapping.kindName }}</h2>
        </FixedRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ mapping.id }}
            </ValueRow>
            <ValueRow label="Version:">
                <VersionDisplay
                    :version-id="mapping.version"
                    :error="mapping.version !== category.systemVersionId"
                />
            </ValueRow>
            <ValueRow label="Root object:">
                <!--
                    TODO - load whole schema category and display the objex's name that corresponds to this key
                -->
                {{ mapping.rootObjexKey }}
            </ValueRow>
            <ValueRow label="Primary key:">
                <SignatureIdDisplay
                    :signature-id="mapping.primaryKey"
                />
            </ValueRow>
            <!--
            <ValueRow label="Datasource:">
                {{ mapping.datasource.label }}
            </ValueRow>
            -->
        </ValueContainer>
        <Divider />
        <ParentPropertyDisplay
            :property="mapping.accessPath"
            :disable-additions="true"
            :class="maxHeightPath && 'max-height'"
        />
    </div>
</template>

<style scoped>
.mapping-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    min-width: 244px;
}

.max-height {
    max-height: 200px;
    overflow-y: auto;
}
</style>
