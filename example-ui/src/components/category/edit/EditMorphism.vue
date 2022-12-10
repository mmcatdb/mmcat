<script lang="ts">
import { Edge, SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { compareCardinalitySettings, type CardinalitySettings } from '@/types/schema';
import { defineComponent } from 'vue';
import CardinalityInput from './CardinalityInput.vue';

import { NodeIndices } from './AddMorphism.vue';

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
        },
        isNew(): boolean {
            return this.edge.schemaMorphism.isNew;
        }
    },
    mounted() {
        if (this.isNew)
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
            this.graph.layout();

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        deleteFunction() {
            // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).

            this.graph.schemaCategory.deleteMorphismWithDual(this.edge.schemaMorphism);
            this.graph.deleteEdgeWithDual(this.edge);

            this.$emit('save');
        },
        unselectAll() {
            this.node1?.unselect();
            this.node2?.unselect();
            this.temporayEdge?.delete();
        },
        selectAll() {
            this.node1?.select({ type: SelectionType.Selected, level: 0 });
            this.node2?.select({ type: SelectionType.Selected, level: 1 });
            if (this.nodesChanged)
                this.temporayEdge = (!!this.node1 && !!this.node2) ? this.graph.createTemporaryEdge(this.node1, this.node2) : null;
        },
        onNodeTapHandler(node: Node): void {
            this.unselectAll();

            let changed = false;
            if (node.equals(this.node1)) {
                this.node1 = null;
                changed = true;
            }

            if (node.equals(this.node2)) {
                this.node2 = null;
                changed = true;
            }

            if (!changed)
                this.handleTapOnNotSelectedNode(node);

            this.selectAll();
        },
        handleTapOnNotSelectedNode(node: Node) {
            // Which node should be changed.
            const changingNodeIndex = this.node1 === null ?
                NodeIndices.First :
                this.node2 === null ?
                    NodeIndices.Second :
                    this.lastSelectedNode;

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
                    Domain object:
                </td>
                <td class="value">
                    {{ node1?.schemaObject.label }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Codomain object:
                </td>
                <td class="value">
                    {{ node2?.schemaObject.label }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Signature:
                </td>
                <td class="value">
                    {{ edge.schemaMorphism.signature }}
                </td>
            </tr>
            <CardinalityInput
                v-model="cardinality"
                :disabled="!isNew"
            />
        </table>
        <div class="button-row">
            <button
                v-if="isNew"
                :disabled="!nodesSelected || !changed"
                @click="save"
            >
                Confirm
            </button>
            <button
                v-if="isNew"
                :disabled="!nodesSelected"
                @click="switchNodes"
            >
                Switch
            </button>
            <button @click="cancel">
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

<style>
.number-input {
    max-width: 80px;
}
</style>

