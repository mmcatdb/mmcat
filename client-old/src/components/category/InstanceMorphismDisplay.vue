<script setup lang="ts">
import { computed } from 'vue';
import type { Edge, Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers/Signature';
import type { InstanceMorphism } from '@/types/instance';
import InstanceObjexHeaderDisplay, { type Column } from './InstanceObjexHeaderDisplay.vue';

const props = defineProps<{
    edge: Edge;
    morphism: InstanceMorphism;
}>();

const emit = defineEmits([ 'objex:click' ]);

const columns = computed<Columns>(() => ({
    dom: props.morphism.dom.schema.superId.signatures.map(signature => defineColumn(signature, props.edge.domainNode)),
    cod: props.morphism.cod.schema.superId.signatures.map(signature => defineColumn(signature, props.edge.codomainNode)),
}));

type Columns = { dom: Column[], cod: Column[] };

function defineColumn(signature: Signature, node: Node): Column {
    const neighbor = node.getNeighborNode(signature);
    return {
        signature,
        schemaObjex: neighbor?.schemaObjex,
        metadata: neighbor?.metadata,
        isClickable: !signature.equals(Signature.empty),
    };
}
</script>

<template>
    <div class="d-flex flex-column p-3">
        <table v-if="morphism.mappings.length > 0">
            <tr>
                <InstanceObjexHeaderDisplay
                    :show-technical-ids="morphism.showDomTechnicalIds"
                    :columns="columns?.dom"
                    @objex:click="(objex) => emit('objex:click', objex)"
                />
                <th class="gap" />
                <InstanceObjexHeaderDisplay
                    :show-technical-ids="morphism.showCodTechnicalIds"
                    :columns="columns?.cod"
                    @objex:click="(objex) => emit('objex:click', objex)"
                />
            </tr>
            <tr
                v-for="(mapping, mappingIndex) in morphism.mappings"
                :key="mappingIndex"
            >
                <td v-if="morphism.showDomTechnicalIds">
                    {{ mapping.dom.technicalIdsString }}
                </td>
                <td
                    v-for="(column, columnIndex) in columns.dom"
                    :key="columnIndex"
                >
                    {{ mapping.dom.superId.tuples.get(column.signature) }}
                </td>
                <td class="gap">
                    &lt;--&gt;
                </td>
                <td v-if="morphism.showCodTechnicalIds">
                    {{ mapping.cod.technicalIdsString }}
                </td>
                <td
                    v-for="(column, columnIndex) in columns.cod"
                    :key="columnIndex"
                >
                    {{ mapping.cod.superId.tuples.get(column.signature) }}
                </td>
            </tr>
        </table>
        <span v-else>
            Instance morphism is empty.
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

th.gap, td.gap {
    padding: 0px 8px;
    background-color: var(--color-background) !important;
    white-space: nowrap;
}
</style>
