<script lang="ts">
import type { Graph, Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import SchemaIds from '@/components/category/SchemaIds.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import AddId from './AddId.vue';

export default defineComponent({
    components: {
        SchemaIds,
        AddId,
        IconPlusSquare
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        node: {
            type: Object as () => Node,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            label: this.node.label,
            key: this.node.schemaObject.key,
            addingId: false
        };
    },
    methods: {
        save() {
            // TODO


            //const object = this.graph.schemaCategory.createObject(this.label, this.key, []);
            //this.graph.createNode(object);



            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        deleteNewNode() {

            // TODO

            this.$emit('save');
        },
        startAddingId() {
            this.addingId = true;
        },
        finishAddingId() {
            this.addingId = false;
        },
        cancelAddingId() {
            this.addingId = false;
        }
    }
});
</script>

<template>
    <div>
        <h2>Edit Schema Object</h2>
        <table>
            <tr>
                <td class="label">
                    Label:
                </td>
                <td class="value">
                    <input
                        v-model="label"
                    />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Key:
                </td>
                <td class="value">
                    {{ key.value }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Ids:
                </td>
                <td class="value">
                    <SchemaIds :schema-object="node.schemaObject" />
                    <span
                        v-if="!addingId"
                        class="button-icon"
                        @click="startAddingId"
                    >
                        <IconPlusSquare />
                    </span>
                </td>
            </tr>
        </table>
        <div
            v-if="addingId"
            class="editor"
        >
            <AddId
                :graph="graph"
                :node="node"
                @save="finishAddingId"
                @cancel="cancelAddingId"
            />
        </div>
        <div class="button-row">
            <button
                :disabled="!label"
                @click="save"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
            <button
                v-if="node.schemaObject.isNew"
                @click="deleteNewNode"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

