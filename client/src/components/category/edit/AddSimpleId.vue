<script setup lang="ts">
import type { PathSegment, Node } from '@/types/categoryGraph';
import { shallowRef } from 'vue';
import { SequenceSignature } from '@/types/accessPath/graph';
import { Cardinality } from '@/types/schema';
import SignatureInput from '../../accessPath/input/SignatureInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureDisplay from '../SignatureDisplay.vue';
import { SignatureId } from '@/types/identifiers';

type AddSimpleIdProps = {
    node: Node;
};

const props = defineProps<AddSimpleIdProps>();

const emit = defineEmits<{
    (e: 'save', signatureId: SignatureId): void;
    (e: 'cancel'): void;
}>();

const sequence = shallowRef(SequenceSignature.empty(props.node));
// It is not possible to require the presence of a dual because the whole principle of the v3 is to make things simpler.
// However, there might be a simple solution that would enforce some morphisms to be bijections.
// TODO
const filter = {
    function: (segment: PathSegment) => segment.direction && segment.edge.schemaMorphism.min === Cardinality.One,
};

function save() {
    const signatureId = new SignatureId([ sequence.value.toSignature() ]);
    emit('save', signatureId);
}

function cancel() {
    emit('cancel');
}
</script>

<template>
    <h2>Add simple Id</h2>
    <ValueContainer>
        <ValueRow label="Signature:">
            <SignatureDisplay :signature="sequence" />
        </ValueRow>
    </ValueContainer>
    <SignatureInput
        v-model="sequence"
        :filter="filter"
    />
    <div class="button-row">
        <button
            :disabled="sequence.isEmpty"
            @click="save"
        >
            Confirm
        </button>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>
