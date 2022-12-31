<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { SchemaObject } from '@/types/schema';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { InstanceObject } from '@/types/instance/InstanceObject';
import type { Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers/Signature';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';
import API from '@/utils/api';

type Column = {
    signature: Signature;
    schemaObject: SchemaObject | undefined;
    isClickable: boolean;
}

type FetchedInstanceObject = {
    object: InstanceObject;
    columns: Column[]
}

interface InstanceObjectProps {
    node: Node;
}

const props = defineProps<InstanceObjectProps>();

const emit = defineEmits([ 'object:click' ]);

const fetchedInstanceObject = ref<FetchedInstanceObject>();
const loading = ref(false);

const schemaCategoryId = useSchemaCategory();

onMounted(async () => {
    loading.value = true;

    const result = await API.instances.getInstanceObject({ categoryId: schemaCategoryId, objectKey: props.node.schemaObject.key.value });
    if (result.status && 'data' in result) {
        const object = InstanceObject.fromServer(result.data);
        fetchedInstanceObject.value = {
            object,
            columns: object.columns.map(signature => ({
                signature,
                schemaObject: props.node.getNeighbour(signature)?.schemaObject,
                isClickable: !signature.equals(Signature.empty)
            }))
        };

        console.log(fetchedInstanceObject.value);
        console.log(props.node);
    }

    loading.value = false;
});

function columnClicked(column: Column) {
    if (column.isClickable)
        emit('object:click', column.schemaObject);
}
</script>

<template>
    <div class="outer">
        <ResourceLoading v-if="loading" />
        <template v-else-if="fetchedInstanceObject">
            <table v-if="fetchedInstanceObject.columns.length > 0">
                <tr>
                    <th
                        v-for="column in fetchedInstanceObject.columns"
                        :key="column.signature.toString()"
                        :class="{ clickable: column.isClickable }"
                        @click="() => columnClicked(column)"
                    >
                        <span class="value">
                            {{ column.schemaObject?.label }}
                        </span>
                        <br />
                        <span class="signature-span">
                            {{ column.signature }}
                        </span>
                    </th>
                </tr>
                <tr
                    v-for="(row, rowIndex) in fetchedInstanceObject.object.rows"
                    :key="rowIndex"
                >
                    <td
                        v-for="(column, columnIndex) in row"
                        :key="columnIndex"
                    >
                        {{ column }}
                    </td>
                </tr>
            </table>
            <span v-else>
                Instance object is empty.
            </span>
        </template>
        <ResourceNotFound v-else />
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

.clickable {
    cursor: pointer;
}
</style>
