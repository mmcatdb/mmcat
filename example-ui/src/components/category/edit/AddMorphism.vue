<script lang="ts">
import { SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality, type CardinalitySettings } from '@/types/schema';
import { defineComponent } from 'vue';
import CardinalityInput from './CardinalityInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

export enum NodeIndices {
    First = 0,
    Second = 1
}

export default defineComponent({
    components: {
        CardinalityInput,
        ValueContainer,
        ValueRow
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            node1: null as Node | null,
            node2: null as Node | null,
            label: '',
            iri: '',
            pimIri: '',
            lastSelectedNode: NodeIndices.First,
            temporayEdge: null as TemporaryEdge | null,
            cardinality: {
                domCodMin: Cardinality.One,
                domCodMax: Cardinality.One,
                codDomMin: Cardinality.One,
                codDomMax: Cardinality.One
            } as CardinalitySettings,
        };
    },
    computed: {
        nodesSelected() {
            return !!this.node1 && !!this.node2;
        },
        iriIsAvailable() {
            return this.graph.schemaCategory.iriIsAvailable(this.iri);
        }
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
    },
    unmounted() {
        this.graph.removeNodeListener('tap', this.onNodeTapHandler);
        this.unselectAll();
    },
    methods: {
        save() {
            if (!this.node1 || !this.node2)
                return;

            if (this.iri) {
                const morphism = this.graph.schemaCategory.createMorphismWithDualWithIri(this.node1.schemaObject, this.node2.schemaObject, this.cardinality, this.iri, this.pimIri, this.label);
                if (!morphism)
                    return;

                this.temporayEdge?.delete();
                this.graph.createEdgeWithDual(morphism, 'new');
            }
            else {
                const morphism = this.graph.schemaCategory.createMorphismWithDual(this.node1.schemaObject, this.node2.schemaObject, this.cardinality, this.label);
                this.temporayEdge?.delete();
                this.graph.createEdgeWithDual(morphism, 'new');
            }

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        unselectAll() {
            this.node1?.unselect();
            this.node2?.unselect();
            this.temporayEdge?.delete();
        },
        selectAll() {
            this.node1?.select({ type: SelectionType.Selected, level: 0 });
            this.node2?.select({ type: SelectionType.Selected, level: 1 });
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
        <h2>Add Schema Morphism</h2>
        <ValueContainer>
            <ValueRow label="Domain object:">
                {{ node1?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Codomain object:">
                {{ node2?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Label?:">
                <input v-model="label" />
            </ValueRow>
            <ValueRow label="Iri?:">
                <input v-model="iri" />
            </ValueRow>
            <ValueRow label="Pim Iri?:">
                <input v-model="pimIri" />
            </ValueRow>
            <CardinalityInput
                v-model="cardinality"
            />
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!nodesSelected || !iriIsAvailable"
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
        </div>
    </div>
</template>

<style>
.number-input {
    max-width: 80px;
}
</style>

