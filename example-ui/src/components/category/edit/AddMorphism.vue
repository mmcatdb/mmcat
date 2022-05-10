<script lang="ts">
import { SelectionType, type Graph, type Node } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers';
import { Cardinality, type CardinalitySettings } from '@/types/schema';
import { defineComponent } from 'vue';
import CardinalityInput from './CardinalityInput.vue';

enum State {
    SelectNode1,
    SelectNode2,
    SelectValue
}

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
            state: State.SelectNode1,
            State,
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
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
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
        confirmNode1() {
            this.state = State.SelectNode2;
        },
        confirmNode2() {
            this.state = State.SelectValue;
        },
        confirmValue() {
            this.save();
        },
        unselectAll() {
            this.node1?.unselect();
            this.node2?.unselect();
        },
        onNodeTapHandler(node: Node): void {
            if (this.state === State.SelectNode1) {
                if (!node.equals(this.node1)) {
                    this.node1?.unselect();
                    node.select({ type: SelectionType.Selected, level: 0 });
                    this.node1 = node;
                }
            }
            else {
                // If node 2 isn't currently selected node 1 and also they aren't neighbours.
                console.log(node.neighbours.get(this.node1 as Node));
                console.log(node.neighbours);
                if (!node.equals(this.node2) && !node.neighbours.get(this.node1 as Node)) {
                    this.node2?.unselect();
                    node.select({ type: SelectionType.Selected, level: 1 });
                    this.node2 = node;
                }
            }
        },
        signatureValueChanged() {
            this.signature = Signature.base(this.signatureValue);
            this.signatureIsValid = this.graph.schemaCategory.isBaseSignatureAvailable(this.signature);
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Add Schema Morphism</h2>
        <template v-if="node1">
            First node: {{ node1.schemaObject.label }}
        </template>
        <template v-else>
            Select first node.
        </template>
        <br />
        <template v-if="state >= State.SelectNode2">
            <template v-if="node2">
                Second node: {{ node2.schemaObject.label }}
            </template>
            <template v-else>
                Select second node.
            </template>
            <br />
        </template>
        <template v-if="state === State.SelectNode1">
            <button
                :disabled="!node1"
                @click="confirmNode1"
            >
                Confirm
            </button>
        </template>
        <template v-else-if="state === State.SelectNode2">
            <button
                :disabled="!node2"
                @click="confirmNode2"
            >
                Confirm
            </button>
        </template>
        <template v-else-if="state === State.SelectValue">
            Signature value:
            <input
                v-model="signatureValue"
                type="number"
                min="0"
                step="1"
                @input="signatureValueChanged"
            />
            <br />
            <CardinalityInput v-model="cardinality" />
            <br />
            <button
                :disabled="!signatureIsValid"
                @click="confirmValue"
            >
                Confirm
            </button>
        </template>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

