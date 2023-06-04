<script setup lang="ts">
import type { PathSegment, Node } from '@/types/categoryGraph';
import { SignatureId, SignatureIdFactory } from '@/types/identifiers';
import { ref, shallowRef } from 'vue';
import { SequenceSignature } from '@/types/accessPath/graph';
import { Cardinality } from "@/types/schema";
import SignatureIdDisplay from '../SignatureIdDisplay.vue';
import SignatureInput from '../../accessPath/input/SignatureInput.vue';
import ButtonIcon from '@/components/ButtonIcon.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureDisplay from '../SignatureDisplay.vue';

type AddComplexIdProps = {
    node: Node;
};

const props = defineProps<AddComplexIdProps>();

const emit = defineEmits<{
    (e: 'save', signatureId: SignatureId): void;
    (e: 'cancel'): void;
}>();

const signatureIdFactory = new SignatureIdFactory();
const signatureId = shallowRef(signatureIdFactory.signatureId);
const addingSignature = ref(false);
const signature = shallowRef(SequenceSignature.empty(props.node));
const idIsNotEmpty = ref(false);

const filter = {
    function: (segment: PathSegment) => segment.direction && segment.edge.schemaMorphism.min === Cardinality.One,
};

function save() {
    emit('save', signatureId.value);
}

function cancel() {
    emit('cancel');
}

function startAddingSignature() {
    signature.value = SequenceSignature.empty(props.node);
    addingSignature.value = true;
    idIsNotEmpty.value = false;
}

function cancelAddingSignature() {
    addingSignature.value = false;
}

function addSignature() {
    signatureId.value = signatureIdFactory.addSignature(signature.value.toSignature());
    addingSignature.value = false;
    idIsNotEmpty.value = true;
}
</script>

<template>
    <h2>Add complex Id</h2>
    <ValueContainer>
        <ValueRow label="Id:">
            <span class="fix-icon-height">
                <SignatureIdDisplay :signature-id="signatureId" />
                <ButtonIcon
                    v-if="!addingSignature"
                    :class="{ 'ml-2': idIsNotEmpty }"
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
                <SignatureDisplay :signature="signature" />
            </ValueRow>
        </ValueContainer>
        <SignatureInput
            v-model="signature"
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
            :disabled="signatureId.signatures.length <= 1"
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

.ml-2 {
    margin-left: 8px;
}
</style>

