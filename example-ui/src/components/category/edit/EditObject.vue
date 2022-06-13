<script lang="ts">
import type { Graph, Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import SchemaIds from '@/components/category/SchemaIds.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import AddId from './AddId.vue';

export default defineComponent({
    expose: [ 'changed' ],
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
    emits: [ 'save', 'cancel', 'update' ],
    data() {
        return {
            label: this.node.schemaObject.label,
            key: this.node.schemaObject.key,
            addingId: false,
            addedId: false
        };
    },
    computed: {
        changed(): boolean {
            // TODO Add a proper condition for an ID change
            return this.label !== this.node.schemaObject.label || this.addingId || this.addedId;
        }
    },
    methods: {
        save() {
            this.node.schemaObject.setLabel(this.label);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        deleteFunction() {
            for (const edge of this.node.neighbours.values()) {
                this.graph.schemaCategory.deleteMorphismWithDual(edge.schemaMorphism);
                this.graph.deleteEdgeWithDual(edge);
            }

            this.graph.schemaCategory.deleteObject(this.node.schemaObject);
            this.graph.deleteNode(this.node);

            this.$emit('save');
        },
        startAddingId() {
            this.addingId = true;
        },
        finishAddingId() {
            this.addingId = false;
            this.addedId = true;
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
                :disabled="!label || !changed || addingId || !node.schemaObject.isNew"
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
                :disabled="!node.schemaObject.isNew"
                @click="deleteFunction"
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

