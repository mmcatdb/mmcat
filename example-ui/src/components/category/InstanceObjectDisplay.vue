<script setup lang="ts">
import { ref } from 'vue';

import ResourceLoader from '@/components/ResourceLoader.vue';
import { InstanceObject } from '@/types/instance/InstanceObject';
import type { Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers/Signature';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';
import API from '@/utils/api';
import type { Column } from './InstanceObjectHeaderDisplay.vue';
import InstanceObjectHeaderDisplay from './InstanceObjectHeaderDisplay.vue';

function defineColumn(signature: Signature, node: Node): Column {
    return {
        signature,
        schemaObject: node.getNeighbour(signature)?.schemaObject,
        isClickable: !signature.equals(Signature.empty)
    };
}

type FetchedInstanceObject = {
    object: InstanceObject;
    columns: Column[];
    showTechnicalIds: boolean;
}

interface InstanceObjectProps {
    node: Node;
}

const props = defineProps<InstanceObjectProps>();

const emit = defineEmits([ 'object:click' ]);

const fetchedInstanceObject = ref<FetchedInstanceObject>();

const schemaCategoryId = useSchemaCategory();

async function fetchObject() {
    const result = await API.instances.getInstanceObject({ categoryId: schemaCategoryId, objectKey: props.node.schemaObject.key.value });
    if (!result.status || !('data' in result))
        return false;

    const object = InstanceObject.fromServer(result.data);
    fetchedInstanceObject.value = {
        object,
        columns: object.superId.signatures.map(signature => defineColumn(signature, props.node)),
        showTechnicalIds: !!object.rows.find(row => row.technicalIds.size > 0)
    };

    return true;
}
</script>

<template>
    <div class="outer">
        <template v-if="fetchedInstanceObject">
            <table v-if="fetchedInstanceObject.object.rows.length > 0">
                <tr>
                    <InstanceObjectHeaderDisplay
                        :show-technical-ids="fetchedInstanceObject.showTechnicalIds"
                        :columns="fetchedInstanceObject.columns"
                        @object:click="(object) => emit('object:click', object)"
                    />
                </tr>
                <tr
                    v-for="(row, rowIndex) in fetchedInstanceObject.object.rows"
                    :key="rowIndex"
                >
                    <td v-if="fetchedInstanceObject.showTechnicalIds">
                        {{ row.technicalIdsString }}
                    </td>
                    <td
                        v-for="(column, columnIndex) in fetchedInstanceObject.columns"
                        :key="columnIndex"
                    >
                        {{ row.superId.tuples.get(column.signature) }}
                    </td>
                </tr>
            </table>
            <span v-else>
                Instance object is empty.
            </span>
        </template>
        <ResourceLoader :loading-function="fetchObject" />
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
</style>
