<script lang="ts">
import { SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers';
import { Cardinality, type CardinalitySettings } from '@/types/schema';
import { defineComponent } from 'vue';
import CardinalityInput from './CardinalityInput.vue';

export default defineComponent({
    components: {
        CardinalityInput
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        const signature = this.graph.schemaCategory.suggestBaseSignature();
        return {
            node1: null as Node | null,
            node2: null as Node | null,
            lastSelectedNode: 1,
            temporayEdge: null as TemporaryEdge | null,
            signature,
            signatureValue: signature.baseValue ?? 0,
            signatureIsValid: true,
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
        }
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
        console.log({ cy: this.graph._cytoscape });
    },
    unmounted() {
        this.graph.removeListener('tap', this.onNodeTapHandler);
    },
    methods: {
        save() {
            // TODO
            if (!this.node1 || !this.node2)
                return;

            const { morphism, dualMorphism } = this.graph.schemaCategory.createMorphism(this.node1.schemaObject, this.node2.schemaObject, this.signature, this.cardinality);
            this.graph.createEdge(morphism, dualMorphism);

            this.unselectAll();
            this.$emit('save');
        },
        cancel() {
            this.unselectAll();
            this.$emit('cancel');
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
            else if (this.node1 === null) {
                node.select({ type: SelectionType.Selected, level: 0 });
                this.node1 = node;
                this.lastSelectedNode = 1;
            }
            else if (this.node2 === null) {
                node.select({ type: SelectionType.Selected, level: 1 });
                this.node2 = node;
                this.lastSelectedNode = 2;
            }
            else if (this.lastSelectedNode === 1) {
                this.node1.unselect();
                node.select({ type: SelectionType.Selected, level: 0 });
                this.node1 = node;
            }
            else {
                this.node2.unselect();
                node.select({ type: SelectionType.Selected, level: 1 });
                this.node2 = node;
            }

            console.log('1: ' + this.node1 + ' 2: ' + this.node2);

            this.temporayEdge?.delete();
            this.temporayEdge = (!!this.node1 && !!this.node2) ? this.graph.createTemporaryEdge(this.node1, this.node2) : null;
        },
        switchNodes() {
            if (this.node1 === null || this.node2 === null)
                return;

            const swap = this.node1;
            this.node1 = this.node2;
            this.node2 = swap;

            this.node1.select({ type: SelectionType.Selected, level: 0 });
            this.node2.select({ type: SelectionType.Selected, level: 1 });
        },
        signatureValueChanged() {
            this.signature = Signature.base(this.signatureValue);
            this.signatureIsValid = this.graph.schemaCategory.isBaseSignatureAvailable(this.signature);
        }
    }
});
</script>

<template>
    <div class="add-morphism">
        <h2>Add Schema Morphism</h2>
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
                        min="0"
                        step="1"
                        class="number-input"
                        @input="signatureValueChanged"
                    />
                </td>
            </tr>
            <CardinalityInput
                v-model="cardinality"
            />
        </table>
        <div class="button-row">
            <button
                :disabled="!nodesSelected || !signatureIsValid"
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

