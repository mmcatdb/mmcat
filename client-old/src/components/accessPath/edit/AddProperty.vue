<script setup lang="ts">
import { GraphSimpleProperty, GraphComplexProperty, SequenceSignature, type GraphParentProperty } from '@/types/accessPath/graph';
import { PropertyType, createDefaultFilter, Node } from '@/types/categoryGraph';
import { StaticName, type Name } from '@/types/identifiers';
import { ref, computed, shallowRef } from 'vue';
import SignatureInput from '../input/SignatureInput.vue';
import TypeInput from '../input/TypeInput.vue';
import NameInput from '../input/NameInput.vue';
import type { Datasource } from '@/types/datasource';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureDisplay from '@/components/category/SignatureDisplay.vue';

enum State {
    SelectSignature,
    SelectType,
    SelectName
}

type AddPropertyProps = {
    datasource: Datasource;
    parentProperty: GraphParentProperty;
};

const props = defineProps<AddPropertyProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const type = ref(PropertyType.Simple);
const signature = shallowRef(SequenceSignature.empty(props.parentProperty.node));
const isAuxiliary = ref(false);
const name = shallowRef<Name>(StaticName.fromString(''));
const state = ref(State.SelectSignature);
const filter = ref(createDefaultFilter(props.datasource.configuration));
const typeIsDetermined = ref(false);

function save() {
    const newProperty = type.value === PropertyType.Simple
        ? new GraphSimpleProperty(name.value, signature.value, props.parentProperty)
        : new GraphComplexProperty(name.value, signature.value, props.parentProperty);

    props.parentProperty.updateOrAddSubpath(newProperty);

    emit('save');
}

function cancel() {
    emit('cancel');
}

// TODO this should probably also change thanks to the auxiliary - EMPTY signature thing.
// However, it's not that trivial. Currently, because of removing of auxiliary nodes before MTC, a complex property with dynamic name can't be auxiliary.
const isSelfIdentifier = computed(() => signature.value.isEmpty && signature.value.sequence.lastNode.schemaObject.idsChecked.isSignatures);

const isSignatureValid = computed(() => {
    if (isAuxiliary.value)
        return signature.value.isEmpty;

    if (signature.value.isEmpty)
        return false;

    if (!props.datasource.configuration.isComplexPropertyAllowed && signature.value.sequence.lastNode.determinedPropertyType === PropertyType.Complex)
        return false;

    return true;
});

const isNameValid = computed(() => !(name.value instanceof StaticName) || !!name.value.value || name.value.isAnonymous);

const isNextButtonDisabled = computed(() => {
    switch (state.value) {
    case State.SelectSignature:
        return !isSignatureValid.value;
    case State.SelectName:
        return !isNameValid.value;
    default:
        return false;
    }
});

function confirmSignature() {
    const node = signature.value.sequence.lastNode;
    const staticNameString = (!signature.value.isEmpty || node.schemaObject.idsChecked.isSignatures) ? node.metadata.label.toLowerCase() : 'id';
    name.value = StaticName.fromString(staticNameString);
    const newType = determinePropertyType(node);

    if (newType !== null) {
        type.value = newType;
        typeIsDetermined.value = true;
        state.value = State.SelectName;
    }
    else {
        state.value = State.SelectType;
        typeIsDetermined.value = false;
    }
}

function determinePropertyType(node: Node): PropertyType | null {
    if (!props.datasource.configuration.isComplexPropertyAllowed)
        return PropertyType.Simple;

    if (isAuxiliary.value)
        return PropertyType.Complex;

    if (isSelfIdentifier.value)
        return PropertyType.Simple;

    return node.determinedPropertyType;
}

function confirmType() {
    state.value = State.SelectName;
}

function confirmName() {
    save();
}

function nextButton() {
    switch (state.value) {
    case State.SelectSignature:
        confirmSignature();
        break;
    case State.SelectType:
        confirmType();
        break;
    case State.SelectName:
        confirmName();
        break;
    }
}

function backButton() {
    state.value--;
    if (state.value === State.SelectType && typeIsDetermined.value)
        state.value--;
}

function isAuxiliaryClicked() {
    signature.value = SequenceSignature.empty(props.parentProperty.node);
}
</script>

<template>
    <div class="outer">
        <h2>Add property</h2>
        <ValueContainer>
            <ValueRow
                v-if="state >= State.SelectSignature && datasource.configuration.isGroupingAllowed"
                label="Is auxiliary:"
            >
                <input
                    v-model="isAuxiliary"
                    :disabled="state > State.SelectSignature"
                    type="checkbox"
                    @input="isAuxiliaryClicked"
                />
            </ValueRow>
            <ValueRow
                v-if="state >= State.SelectSignature && !isAuxiliary"
                label="Signature:"
            >
                <SignatureDisplay :signature="signature" />
            </ValueRow>
            <ValueRow
                v-if="state >= State.SelectName"
                label="Type:"
            >
                {{ type }}
            </ValueRow>
            <ValueRow
                v-if="state === State.SelectType"
                label="Type:"
            >
                <TypeInput v-model="type" />
            </ValueRow>
            <ValueRow
                v-if="state === State.SelectName"
                label="Name:"
            >
                <NameInput
                    v-model="name"
                    :datasource="datasource"
                    :root-node="parentProperty.node"
                    :is-self-identifier="isSelfIdentifier"
                />
            </ValueRow>
        </ValueContainer>
        <SignatureInput
            v-if="state === State.SelectSignature && !isAuxiliary"
            v-model="signature"
            :filter="filter"
        />
        <div class="button-row">
            <button
                :disabled="isNextButtonDisabled"
                @click="nextButton"
            >
                {{ state < State.SelectName ? 'Next' : 'Finish' }}
            </button>
            <button
                v-if="state > State.SelectSignature"
                @click="backButton"
            >
                Back
            </button>
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </div>
</template>
