<script setup lang="ts">
import { computed } from 'vue';
import type { InstanceObjex } from '@/types/instance/InstanceObjex';
import type { Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers/Signature';
import type { Column } from './InstanceObjexHeaderDisplay.vue';
import InstanceObjectHeaderDisplay from './InstanceObjexHeaderDisplay.vue';

const props = defineProps<{
    node: Node;
    objex: InstanceObjex;
}>();

const emit = defineEmits([ 'objex:click' ]);

function defineColumn(signature: Signature, node: Node): Column {
    const neighbor = node.getNeighborNode(signature);
    return {
        signature,
        schemaObjex: neighbor?.schemaObjex,
        metadata: neighbor?.metadata,
        isClickable: !signature.equals(Signature.empty),
    };
}

const showTechnicalIds = computed(() => !!props.objex.rows.find(row => row.technicalIds.size > 0));
const columns = props.objex.schema.superId.signatures.map(signature => defineColumn(signature, props.node));
</script>

<template>
    <div class="d-flex flex-column p-3">
        <table v-if="objex.rows.length > 0">
            <tr>
                <InstanceObjectHeaderDisplay
                    :show-technical-ids="showTechnicalIds"
                    :columns="columns"
                    @objex:click="(objex) => emit('objex:click', objex)"
                />
            </tr>
            <tr
                v-for="(row, rowIndex) in objex.rows"
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
