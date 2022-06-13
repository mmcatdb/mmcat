<script lang="ts">
import { Edge, SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { compareCardinalitySettings, type CardinalitySettings } from '@/types/schema';
import { defineComponent } from 'vue';
import CardinalityInput from './CardinalityInput.vue';

import { NodeIndices, negateIndex } from './AddMorphism.vue';

export default defineComponent({
    components: {
        CardinalityInput
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        edge: {
            type: Object as () => Edge,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            node1: null as Node | null,
            node2: null as Node | null,
            lastSelectedNode: NodeIndices.First,
            temporayEdge: null as TemporaryEdge | null,
            signatureValue: this.edge.schemaMorphism.signature.baseValue ?? 0,
            cardinality: {
                domCodMin: this.edge.schemaMorphism.min,
                domCodMax: this.edge.schemaMorphism.max,
                codDomMin: this.edge.schemaMorphism.dual.min,
                codDomMax: this.edge.schemaMorphism.dual.max
            } as CardinalitySettings,
        };
    },
    computed: {
        nodesSelected() {
            return !!this.node1 && !!this.node2;
        },
        changed(): boolean {
            return !this.edge.domainNode.equals(this.node1) || !this.edge.codomainNode.equals(this.node2) || !compareCardinalitySettings(this.cardinality, {
                domCodMin: this.edge.schemaMorphism.min,
                domCodMax: this.edge.schemaMorphism.max,
                codDomMin: this.edge.schemaMorphism.dual.min,
                codDomMax: this.edge.schemaMorphism.dual.max
            });
        },
        nodesChanged(): boolean {
            return !this.edge.domainNode.equals(this.node1) || !this.edge.codomainNode.equals(this.node2);
        }
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
        this.onNodeTapHandler(this.edge.domainNode);
        this.onNodeTapHandler(this.edge.codomainNode);
    },
    unmounted() {
        this.graph.removeNodeListener('tap', this.onNodeTapHandler);
        this.unselectAll();
    },
    methods: {
        save() {
            if (!this.node1 || !this.node2)
                return;

            // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).

            this.graph.schemaCategory.editMorphismWithDual(this.edge.schemaMorphism, this.node1.schemaObject, this.node2.schemaObject, this.cardinality);

            this.temporayEdge?.delete();
            this.graph.deleteEdgeWithDual(this.edge);
            this.graph.createEdgeWithDual(this.edge.schemaMorphism, 'new');

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        deleteFunction() {
            this.graph.schemaCategory.deleteMorphismWithDual(this.edge.schemaMorphism);
            this.graph.deleteEdgeWithDual(this.edge);

            this.$emit('save');
        },
        unselectAll() {
            this.node1?.unselect();
            this.node2?.unselect();
            this.temporayEdge?.delete();
        },
        onNodeTapHandler(node: Node): void {
            if (node.equals(this.node1)) {
                node.unselect();
                this.node1 = null;
            }
            else if (node.equals(this.node2)) {
                node.unselect();
                this.node2 = null;
            }
            else {
                this.handleTapOnNotSelectedNode(node);
            }

            this.temporayEdge?.delete();
            if (this.nodesChanged)
                this.temporayEdge = (!!this.node1 && !!this.node2) ? this.graph.createTemporaryEdge(this.node1, this.node2) : null;
        },
        handleTapOnNotSelectedNode(node: Node) {
            // Which node should be changed.
            const changingNodeIndex = this.node1 === null ?
                NodeIndices.First :
                this.node2 === null ?
                    NodeIndices.Second :
                    this.lastSelectedNode;

            const changingNode = this.indexToNode(changingNodeIndex);
            const stationaryNode = this.indexToNode(negateIndex(changingNodeIndex));

            if (this.morphismAlreadyExists(node, stationaryNode) && !node.equals(this.edge.domainNode) && !node.equals(this.edge.codomainNode))
                return;

            changingNode?.unselect();
            node.select({ type: SelectionType.Selected, level: changingNodeIndex });
            this.setNodeOnIndex(node, changingNodeIndex);

            this.lastSelectedNode = changingNodeIndex;
        },
        indexToNode(index: NodeIndices): Node | null {
            return index === NodeIndices.First ? this.node1 : this.node2;
        },
        setNodeOnIndex(node: Node, index: NodeIndices) {
            if (index === NodeIndices.First)
                this.node1 = node;
            else
                this.node2 = node;
        },
        morphismAlreadyExists(node1: Node | null, node2: Node | null): boolean {
            if (!node1 || !node2)
                return false;

            if (node1.equals(this.edge.codomainNode) && node2.equals(this.edge.domainNode))
                return false;

            return !!node1.neighbours.get(node2);
        },
        switchNodes() {
            if (this.node1 === null || this.node2 === null)
                return;

            const swap = this.node1;
            this.node1 = this.node2;
            this.node2 = swap;

            this.node1.select({ type: SelectionType.Selected, level: 0 });
            this.node2.select({ type: SelectionType.Selected, level: 1 });
        }
    }
});
</script>

<template>
    <div class="add-morphism">
        <h2>Edit Schema Morphism</h2>
        <table>
            <tr>
                <td class="label">
                    First node:
                </td>
                <td class="value">
                    {{ node1?.schemaObject.label }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Second node:
                </td>
                <td class="value">
                    {{ node2?.schemaObject.label }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Signature value:
                </td>
                <td class="value">
                    <input
                        v-model="signatureValue"
                        type="number"
                        class="number-input"
                        disabled
                    />
                </td>
            </tr>
            <CardinalityInput
                v-model="cardinality"
            />
        </table>
        <div class="button-row">
            <button
                :disabled="!nodesSelected || !changed || !edge.schemaMorphism.isNew"
                @click="save"
            >
                Confirm
            </button>
            <button
                :disabled="!nodesSelected"
                @click="switchNodes"
            >
                Switch
            </button>
            <button @click="cancel">
                Cancel
            </button>
            <button
                :disabled="!edge.schemaMorphism.isNew"
                @click="deleteFunction"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style>
.number-input {
    max-width: 80px;
}
</style>

