<script setup lang="ts">
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import type { DataSource } from '@/types/dataSource';
import IriDisplay from '../IriDisplay.vue';

interface DataSourceDisplayProps {
    dataSource: DataSource;
}

defineProps<DataSourceDisplayProps>();

const emit = defineEmits([ 'edit' ]);

function edit() {
    emit('edit');
}
</script>

<template>
    <div class="data-source-display">
        <CleverRouterLink :to="{ name: 'dataSource', params: { id: dataSource.id } }">
            <h2>{{ dataSource.label }}</h2>
        </CleverRouterLink>
        <table>
            <tr>
                <td class="label">
                    Id:
                </td>
                <td class="value">
                    {{ dataSource.id }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    {{ dataSource.type }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Url:
                </td>
                <td class="value">
                    <IriDisplay
                        :iri="dataSource.url"
                        :max-chars="36"
                    />
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
.data-source-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 204px;
}
</style>
