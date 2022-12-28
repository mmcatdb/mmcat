<script lang="ts">
import type { Graph, PathSegment, Node } from '@/types/categoryGraph';
import { SignatureIdFactory } from '@/types/identifiers';
import { defineComponent } from 'vue';
import { SequenceSignature } from '@/types/accessPath/graph';
import { Cardinality } from "@/types/schema";
import SignatureIdDisplay from '../SignatureIdDisplay.vue';
import SignatureInput from '../../accessPath/input/SignatureInput.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';

export default defineComponent({
    components: {
        SignatureIdDisplay,
        SignatureInput,
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
            signatureIdFactory: new SignatureIdFactory(),
            addingSignature: false,
            signature: SequenceSignature.empty(this.node),
            idIsNotEmpty: false,
            filter: {
                function:
                    (segment: PathSegment) =>
                        segment.edge.schemaMorphism.min === Cardinality.One
                        && segment.edge.schemaMorphism.max === Cardinality.One
                // TODO make the id invalid later
                // && segment.dual.max === Cardinality.Star;
            }
        };
    },
    methods: {
        save() {
            this.node.addSignatureId(this.signatureIdFactory.signatureId);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        startAddingSignature() {
            this.signature = SequenceSignature.empty(this.node);
            this.addingSignature = true;
            this.idIsNotEmpty = false;
        },
        cancelAddingSignature() {
            this.addingSignature = false;
        },
        addSignature() {
            this.signatureIdFactory.addSignature(this.signature.toSignature());
            this.addingSignature = false;
            this.idIsNotEmpty = true;
        }
    }
});
</script>

<template>
    <h2>Add complex Id</h2>
    <table>
        <tr>
            <td class="label">
                Id:
            </td>
            <td class="value fix-icon-height">
                <SignatureIdDisplay :signature-id="signatureIdFactory.signatureId" />
                <span
                    v-if="!addingSignature"
                    class="button-icon"
                    :class="{ 'ml-2': !signatureIdFactory.isEmpty }"
                    @click="startAddingSignature"
                >
                    <IconPlusSquare />
                </span>
            </td>
        </tr>
    </table>
    <div
        v-if="addingSignature"
        class="editor"
    >
        <h2>Add signature</h2>
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
                @click="addSignature"
            >
                Confirm
            </button>
            <button @click="cancelAddingSignature">
                Cancel
            </button>
        </div>
    </div>
    <div class="button-row">
        <button
            :disabled="signatureIdFactory.length <= 1"
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
.signature-span {
    background-color: var(--color-primary-dark);
    border-radius: 4px;
    padding: 0px 6px 0px 4px;
    font-weight: bold;
}

.comma-span {
    margin-right: 8px;
    margin-left: 2px;
}

.fix-icon-height {
    display: inline-flex;
}

.fix-icon-height > .button-icon {
    max-height: 20px;
}

.fix-icon-height svg.icon {
    top: 2px;
}

.ml-2 {
    margin-left: 8px;
}
</style>

