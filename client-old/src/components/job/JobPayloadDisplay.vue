<script setup lang="ts">
import { ActionType, type JobPayload } from '@/types/action';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';

defineProps<{
    payload: JobPayload;
    withLabels?: boolean;
}>();
</script>

<template>
    <div>
        <template v-if="payload.type === ActionType.UpdateSchema">
            <VersionDisplay :version-id="payload.prevVersion" /> -> <VersionDisplay :version-id="payload.nextVersion" />
        </template>
        <div v-else-if="payload.type === ActionType.CategoryToModel || payload.type === ActionType.ModelToCategory">
            <span
                v-if="withLabels"
                class="text-bold me-2"
            >
                Datasource:
            </span>
            <FixedRouterLink :to="{ name: 'datasource', params: { id: payload.datasource.id } }">
                {{ payload.datasource.label }}
            </FixedRouterLink>
            <div
                v-if="payload.mappings.length > 0"
                class="d-flex flex-wrap"
            >
                <span
                    v-if="withLabels"
                    class="text-bold me-2"
                >
                    Mappings:
                </span>
                <span
                    v-for="(mapping, index) in payload.mappings"
                    :key="mapping.id"
                >
                    <FixedRouterLink 
                        :to="{ name: 'mapping', params: {id: mapping.id } }"
                    >
                        {{ mapping.kindName }}
                    </FixedRouterLink>
                    <span
                        v-if="index !== payload.mappings.length - 1"
                        class="px-1"
                    >,</span>
                </span>
            </div>
        </div>
        <template v-else>
            <span
                v-if="withLabels"
                class="text-bold me-2"
            >
                Datasources:
            </span>
            <FixedRouterLink
                v-for="datasource in payload.datasources"
                :key="datasource.id"
                :to="{ name: 'datasource', params: { id: datasource.id } }"
            >
                {{ datasource.label }}
            </FixedRouterLink>
        </template>
    </div>
</template>
