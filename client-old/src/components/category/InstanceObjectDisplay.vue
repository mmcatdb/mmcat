<script setup lang="ts">
import { computed } from 'vue';
import type { InstanceObject } from '@/types/instance/InstanceObject';
import type { Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers/Signature';
import type { Column } from './InstanceObjectHeaderDisplay.vue';
import InstanceObjectHeaderDisplay from './InstanceObjectHeaderDisplay.vue';

const props = defineProps<{
    node: Node;
    object: InstanceObject;
}>();

const emit = defineEmits([ 'object:click' ]);

function defineColumn(signature: Signature, node: Node): Column {
    const neighbor = node.getNeighborNode(signature);
    return {
        signature,
        schemaObjex: neighbor?.schemaObjex,
        metadata: neighbor?.metadata,
        isClickable: !signature.equals(Signature.empty),
    };
}

const showTechnicalIds = computed(() => !!props.object.rows.find(row => row.technicalIds.size > 0));
const columns = props.object.schema.superId.signatures.map(signature => defineColumn(signature, props.node));
</script>

<template>
    <div class="d-flex flex-column p-3">
        <table v-if="object.rows.length > 0">
            <tr>
                <InstanceObjectHeaderDisplay
                    :show-technical-ids="showTechnicalIds"
                    :columns="columns"
                    @object:click="(object) => emit('object:click', object)"
                />
            </tr>
            <tr
                v-for="(row, rowIndex) in object.rows"
                :key="rowIndex"
            >
                <td v-if="showTechnicalIds">
                    {{ row.technicalIdsString }}
                </td>
                <td
                    v-for="(column, columnIndex) in columns"
                    :key="columnIndex"
                >
                    {{ row.superId.tuples.get(column.signature) }}
                </td>
            </tr>
        </table>
        <span v-else>
            Instance object is empty.
        </span>
    </div>
</template>

<style scoped>
tr {
    padding: 8px;
}

td, th {
    padding: 0 16px;
    background-color: var(--color-background-mute);
}

tr:nth-of-type(2n) td {
    background-color: var(--vt-c-black-soft);
}
</style>
