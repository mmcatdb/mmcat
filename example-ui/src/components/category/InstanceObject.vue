<script lang="ts">
import { defineComponent } from 'vue';
import { GET } from '@/utils/backendAPI';
import type { SchemaObject } from '@/types/schema';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { InstanceObject, type InstanceObjectFromServer } from '@/types/instance/InstanceObject';
import type { Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers/Signature';

type Column = {
    signature: Signature;
    schemaObject: SchemaObject | undefined;
    isClickable: boolean;
}

type FetchedInstanceObject = {
    object: InstanceObject;
    columns: Column[]
}

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading
    },
    props: {
        node: {
            type: Object as () => Node,
            required: true
        },
    },
    emits: [ 'object:click' ],
    data() {
        return {
            fetchedInstanceObject: null as FetchedInstanceObject | null,
            loading: false
        };
    },
    watch: {
        node: {
            handler(): void {
                this.reloadInstanceObject();
            }
        }
    },
    mounted() {
        this.reloadInstanceObject();
    },
    methods: {
        async reloadInstanceObject() {
            this.loading = true;

            const result = await GET<InstanceObjectFromServer>(`/instances/default/object/${this.node.schemaObject.key.value}`);
            if (result.status && 'data' in result) {
                const object = InstanceObject.fromServer(result.data);
                this.fetchedInstanceObject = {
                    object,
                    columns: object.columns.map(signature => ({
                        signature,
                        schemaObject: this.node.getNeighbour(signature)?.schemaObject,
                        isClickable: !signature.equals(Signature.empty)
                    }))
                };

                console.log(this.fetchedInstanceObject);
                console.log(this.node);
            }

            this.loading = false;
        },
        columnClicked(column: Column) {
            if (column.isClickable)
                this.$emit('object:click', column.schemaObject);
        }
    }
});
</script>

<template>
    <div class="outer">
        <ResourceLoading v-if="loading" />
        <template v-else-if="fetchedInstanceObject">
            <table v-if="fetchedInstanceObject.columns.length > 0">
                <tr>
                    <th
                        v-for="(column, index) in fetchedInstanceObject.columns"
                        :key="index"
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

.value {
    font-weight: bold;
}
</style>
