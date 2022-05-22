<script lang="ts">
import type { Graph, NodeNeighbour, Node } from '@/types/categoryGraph';
import { SchemaIdFactory } from '@/types/identifiers';
import { defineComponent } from 'vue';
import { SequenceSignature } from '@/types/accessPath/graph';
import { Cardinality } from "@/types/schema";
import SignatureInput from '../../accessPath/input/SignatureInput.vue';

export default defineComponent({
    components: {
        SignatureInput,
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
            signature: SequenceSignature.empty(this.node)
        };
    },
    methods: {
        save() {
            const factory = new SchemaIdFactory([ this.signature.toSignature() ]);
            this.node.addId(factory.schemaId);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        onlyOneToOnePathFilter(neighbour: NodeNeighbour): boolean {
            return neighbour.morphism.min === Cardinality.One && neighbour.morphism.max === Cardinality.One;
        }
    }
});
</script>

<template>
    <h2>Add simple Id</h2>
    <table>
        <tr>
            <td class="label">
                Signature:
            </td>
            <td class="value">
                {{ signature }}
            </td>
        </tr>
    </table>
    <SignatureInput
        v-model="signature"
        :graph="graph"
        :constraint="{ filter: onlyOneToOnePathFilter }"
    />
    <div class="button-row">
        <button
            :disabled="signature.isEmpty"
            @click="save"
        >
            Confirm
        </button>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>

<style scoped>

</style>
