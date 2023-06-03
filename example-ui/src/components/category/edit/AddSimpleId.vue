<script setup lang="ts">
import type { PathSegment, Node } from '@/types/categoryGraph';
import { shallowRef } from 'vue';
import { SequenceSignature } from '@/types/accessPath/graph';
import { Cardinality } from "@/types/schema";
import SignatureInput from '../../accessPath/input/SignatureInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureDisplay from '../SignatureDisplay.vue';
import { useEvocat } from '@/utils/injects';

const { evocat } = $(useEvocat());

type AddSimpleIdProps = {
    node: Node;
};

const props = defineProps<AddSimpleIdProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const signature = shallowRef(SequenceSignature.empty(props.node));
// It is not possible to require the presence of a dual because the whole principle of the v3 is to make things simpler.
// However, there might be a simple solution that would enforce some morphisms to be bijections.
// TODO
const filter = {
    function: (segment: PathSegment) => segment.direction && segment.edge.schemaMorphism.min === Cardinality.One,
};

function save() {
    evocat.createId(props.node.schemaObject, { signatures: [ signature.value.toSignature() ] });

    emit('save');
}

function cancel() {
    emit('cancel');
}
</script>

<template>
    <h2>Add simple Id</h2>
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
            @click="save"
        >
            Confirm
        </button>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>
