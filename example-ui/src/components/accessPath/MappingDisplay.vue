<script lang="ts">
import { defineComponent } from 'vue';
import type { Mapping } from '@/types/mapping';
import ParentPropertyDisplay from './display/ParentPropertyDisplay.vue';
import Divider from '@/components/layout/Divider.vue';

export default defineComponent({
    components: {
        ParentPropertyDisplay,
        Divider
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
        <!--
        <CleverRouterLink :to="{ name: 'mapping', params: { id: mapping.id } }">
            <h2>{{ mapping.name }}</h2>
        </CleverRouterLink>
        -->
        <h2>{{ mapping.label }}</h2>
        <table>
            <tr>
                <td class="label">
                    Id:
                </td>
                <td class="value">
                    {{ mapping.id }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Root object id:
                </td>
                <td class="value">
                    {{ mapping.rootObjectId }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Logical model:
                </td>
                <td class="value">
                    {{ mapping.logicalModel.label }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Database:
                </td>
                <td class="value">
                    {{ mapping.logicalModel.databaseView.label }}
                </td>
            </tr>
        </table>
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
