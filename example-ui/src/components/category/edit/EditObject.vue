<script lang="ts">
import type { Graph, Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import ObjectIdsDisplay from '@/components/category/ObjectIdsDisplay.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import ButtonIcon from '@/components/ButtonIcon.vue';
import AddId from './AddId.vue';
import { Type } from '@/types/identifiers';

export default defineComponent({
    expose: [ 'changed' ],
    components: {
        ObjectIdsDisplay,
        AddId,
        ButtonIcon,
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
            addingId: false,
            Type
        };
    },
    computed: {
        changed(): boolean {
            //return this.label !== this.node.schemaObject.label || this.addingId || this.addedId;
            return this.label !== this.node.schemaObject.label || this.addingId;
        },
        isNew(): boolean {
            return this.node.schemaObject.isNew;
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
            this.node.adjacentEdges.forEach(edge => {
                this.graph.schemaCategory.deleteMorphismWithDual(edge.schemaMorphism);
                this.graph.deleteEdgeWithDual(edge);
            });

            this.graph.schemaCategory.deleteObject(this.node.schemaObject);
            this.graph.deleteNode(this.node);

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
                        :disabled="!isNew"
                    />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Key:
                </td>
                <td class="value">
                    {{ node.schemaObject.key.value }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Ids:
                </td>
                <td class="value">
                    <ObjectIdsDisplay
                        v-if="node.schemaObject.ids"
                        :ids="node.schemaObject.ids"
                        :disabled="!isNew"
                        @delete-signature="(index) => node.deleteSignatureId(index)"
                        @delete-non-signature="() => node.deleteNonSignatureId()"
                    />
                    <ButtonIcon
                        v-if="!addingId && isNew && (!node.schemaObject.ids || node.schemaObject.ids.type === Type.Signatures)"
                        @click="startAddingId"
                    >
                        <IconPlusSquare />
                    </ButtonIcon>
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
                v-if="isNew"
                :disabled="!label || !changed || addingId"
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
                v-if="isNew"
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

