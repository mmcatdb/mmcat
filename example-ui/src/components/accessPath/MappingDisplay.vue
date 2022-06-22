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
        <RouterLink :to="{ name: 'mapping', params: { id: mapping.id } }">
            <h2>{{ mapping.name }}</h2>
        </RouterLink>
        -->
        <h2>{{ mapping.name }}</h2>
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
                    Database:
                </td>
                <td class="value">
                    {{ mapping.databaseView.label }}
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
