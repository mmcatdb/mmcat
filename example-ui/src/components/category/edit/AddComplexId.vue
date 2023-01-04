<script lang="ts">
import type { Graph, PathSegment, Node } from '@/types/categoryGraph';
import { SignatureIdFactory } from '@/types/identifiers';
import { defineComponent } from 'vue';
import { SequenceSignature } from '@/types/accessPath/graph';
import { Cardinality } from "@/types/schema";
import SignatureIdDisplay from '../SignatureIdDisplay.vue';
import SignatureInput from '../../accessPath/input/SignatureInput.vue';
import ButtonIcon from '@/components/ButtonIcon.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

export default defineComponent({
    components: {
        SignatureIdDisplay,
        SignatureInput,
        ButtonIcon,
        IconPlusSquare,
        ValueContainer,
        ValueRow
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
    <ValueContainer>
        <ValueRow label="Id:">
            <span class="fix-icon-height">
                <SignatureIdDisplay :signature-id="signatureIdFactory.signatureId" />
                <ButtonIcon
                    v-if="!addingSignature"
                    :class="{ 'ml-2': !signatureIdFactory.isEmpty }"
                    @click="startAddingSignature"
                >
                    <IconPlusSquare />
                </ButtonIcon>
            </span>
        </ValueRow>
    </ValueContainer>
    <div
        v-if="addingSignature"
        class="editor"
    >
        <h2>Add signature</h2>
        <ValueContainer>
            <ValueRow label="Signature:">
                {{ signature }}
            </ValueRow>
        </ValueContainer>
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
.comma-span {
    margin-right: 8px;
    margin-left: 2px;
}

.fix-icon-height {
    display: inline-flex;
}

.fix-icon-height .button-icon {
    max-height: 20px;
}

.fix-icon-height svg.icon {
    top: 2px;
}

.ml-2 {
    margin-left: 8px;
}
</style>

