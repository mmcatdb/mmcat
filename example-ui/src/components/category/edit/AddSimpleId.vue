<script lang="ts">
import type { Graph, PathSegment, Node } from '@/types/categoryGraph';
import { SignatureIdFactory } from '@/types/identifiers';
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
            signature: SequenceSignature.empty(this.node),
            filter: {
                function: (segment: PathSegment) =>
                    segment.edge.schemaMorphism.min === Cardinality.One
                        && segment.edge.schemaMorphism.max === Cardinality.One
                        && segment.edge.schemaMorphism.dual.max === Cardinality.One
            }
        };
    },
    methods: {
        save() {
            const factory = new SignatureIdFactory([ this.signature.toSignature() ]);
            this.node.addSignatureId(factory.signatureId);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
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
        :filter="filter"
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
