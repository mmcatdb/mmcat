<script setup lang="ts">
import { ref } from 'vue';
import type { SchemaObject } from '@/types/schema';

import ResourceLoader from '@/components/ResourceLoader.vue';
import type { Edge, Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers/Signature';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';
import API from '@/utils/api';
import { InstanceMorphism } from '@/types/instance';
import InstanceObjectHeaderDisplay from './InstanceObjectHeaderDisplay.vue';

type InstanceObjectProps = {
    edge: Edge;
};

const props = defineProps<InstanceObjectProps>();

const emit = defineEmits([ 'object:click' ]);

const morphism = ref<InstanceMorphism>();
const columns = ref<Columns>();

type Columns = { dom: Column[], cod: Column[] };

type Column = {
    signature: Signature;
    schemaObject: SchemaObject | undefined;
    isClickable: boolean;
};

function defineColumn(signature: Signature, node: Node): Column {
    return {
        signature,
        schemaObject: node.getNeighbour(signature)?.schemaObject,
        isClickable: !signature.equals(Signature.empty)
    };
}

const categoryId = useSchemaCategory();

async function fetchMorphism() {
    const result = await API.instances.getInstanceMorphism({ categoryId, signature: props.edge.schemaMorphism.signature.toString() });
    if (!result.status)
        return false;

    morphism.value = InstanceMorphism.fromServer(result.data);
    columns.value = {
        dom: morphism.value.domSuperId.signatures.map(signature => defineColumn(signature, props.edge.domainNode)),
        cod: morphism.value.codSuperId.signatures.map(signature => defineColumn(signature, props.edge.codomainNode))
    };

    return true;
}
</script>

<template>
    <div class="outer">
        <template v-if="morphism && columns">
            <table v-if="morphism.mappings.length > 0">
                <tr>
                    <InstanceObjectHeaderDisplay
                        :show-technical-ids="morphism.showDomTechnicalIds"
                        :columns="columns?.dom"
                        @object:click="(object) => emit('object:click', object)"
                    />
                    <th class="gap" />
                    <InstanceObjectHeaderDisplay
                        :show-technical-ids="morphism.showCodTechnicalIds"
                        :columns="columns?.cod"
                        @object:click="(object) => emit('object:click', object)"
                    />
                </tr>
                <tr
                    v-for="(mapping, mappingIndex) in morphism.mappings"
                    :key="mappingIndex"
                >
                    <td v-if="morphism.showDomTechnicalIds">
                        {{ mapping.domRow.technicalIdsString }}
                    </td>
                    <td
                        v-for="(column, columnIndex) in columns.dom"
                        :key="columnIndex"
                    >
                        {{ mapping.domRow.superId.tuples.get(column.signature) }}
                    </td>
                    <td class="gap">
                        &lt;--&gt;
                    </td>
                    <td v-if="morphism.showCodTechnicalIds">
                        {{ mapping.codRow.technicalIdsString }}
                    </td>
                    <td
                        v-for="(column, columnIndex) in columns.cod"
                        :key="columnIndex"
                    >
                        {{ mapping.codRow.superId.tuples.get(column.signature) }}
                    </td>
                </tr>
            </table>
            <span v-else>
                Instance object is empty.
            </span>
        </template>
        <ResourceLoader :loading-function="fetchMorphism" />
    </div>
</template>

<style scoped>
.outer {
    display: flex;
    flex-direction: column;
    padding: 16px;
}

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
