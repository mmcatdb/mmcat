<script setup lang="ts">
import { GraphSimpleProperty, GraphComplexProperty, type GraphChildProperty } from '@/types/accessPath/graph';
import { PropertyType, createDefaultFilter, type Node } from '@/types/categoryGraph';
import { StaticName, type Name } from '@/types/identifiers';
import { ref, computed, shallowRef } from 'vue';
import type { Datasource } from '@/types/datasource';
import SignatureInput from '../input/SignatureInput.vue';
import TypeInput from '../input/TypeInput.vue';
import NameInput from '../input/NameInput.vue';
import ObjexIdsDisplay from '@/components/category/ObjexIdsDisplay.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SignatureDisplay from '@/components/category/SignatureDisplay.vue';

enum State {
    SelectSignature,
    SelectType,
    SelectName
}

type EditPropertyProps = {
    datasource: Datasource;
    property: GraphChildProperty;
};

const props = defineProps<EditPropertyProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const type = ref(propertyToType(props.property));
const signature = shallowRef(props.property.signature.copy());
const name = shallowRef<Name>(props.property.name.copy());
const state = ref(State.SelectSignature);
const filter = ref(createDefaultFilter(props.datasource.configuration));
const typeIsDetermined = ref(false);

const typeChanged = computed(() => type.value !== propertyToType(props.property));
const nameChanged = computed(() => !props.property.name.equals(name.value));
const signatureChanged = computed(() => !props.property.signature.equals(signature.value));
const lastNode = computed(() => signature.value.sequence.lastNode);

function propertyToType(property: GraphChildProperty): PropertyType {
    return property instanceof GraphSimpleProperty ? PropertyType.Simple : PropertyType.Complex;
}

function save() {
    const subpaths = !signatureChanged.value && !typeChanged.value && props.property instanceof GraphComplexProperty ? props.property.subpaths : [];
    const newProperty = type.value === PropertyType.Simple
        ? new GraphSimpleProperty(name.value, signature.value, props.property.parent)
        : new GraphComplexProperty(name.value, signature.value, props.property.parent, subpaths);

    props.property.parent.updateOrAddSubpath(newProperty, props.property);

    emit('save');
}

function cancel() {
    emit('cancel');
}

const isSelfIdentifier = computed(() => signature.value.isEmpty && !lastNode.value.schemaObjex.idsChecked.isSignatures);

const isSignatureValid = computed(() => {
    if (!props.datasource.configuration.isComplexPropertyAllowed && lastNode.value.determinedPropertyType === PropertyType.Complex)
        return false;

    return true;
});

const isNameValid = computed(() => !(name.value instanceof StaticName) || !!name.value.value);

function confirmSignature() {
    const node = lastNode.value;
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

    if (signature.value.isEmpty)
        return PropertyType.Complex;

    if (isSelfIdentifier.value)
        return PropertyType.Simple;

    return node.determinedPropertyType;
}

function resetSignature() {
    signature.value = props.property.signature.copy();
}

function confirmType() {
    state.value = State.SelectName;
}

function resetType() {
    type.value = propertyToType(props.property);
}

function confirmName() {
    save();
}

function resetName() {
    name.value = props.property.name.copy();
}

function deleteProperty() {
    props.property.parent.removeSubpath(props.property);
    emit('save');
}

function backButton() {
    state.value--;
    if (state.value === State.SelectType && typeIsDetermined.value)
        state.value--;
}
</script>

<template>
    <div class="outer">
        <h2>Edit property</h2>
        <ValueContainer>
            <ValueRow label="Object:">
                {{ lastNode.metadata.label }}
            </ValueRow>
            <ValueRow label="Ids:">
                <ObjexIdsDisplay
                    v-if="lastNode.schemaObjex.ids"
                    :ids="lastNode.schemaObjex.ids"
                    disabled
                    class="objex-ids-display"
                />
            </ValueRow>
            <ValueRow
                v-if="state >= State.SelectSignature"
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
                    :root-node="property.parentNode"
                    :is-self-identifier="isSelfIdentifier"
                />
            </ValueRow>
        </ValueContainer>
        <SignatureInput
            v-if="state === State.SelectSignature"
            v-model="signature"
            :filter="filter"
        />
        <div class="button-row">
            <template v-if="state === State.SelectSignature">
                <button
                    :disabled="signatureChanged && !isSignatureValid"
                    @click="confirmSignature"
                >
                    {{ signatureChanged ? 'Confirm change' : 'Keep current' }}
                </button>
                <button
                    v-if="signatureChanged"
                    @click="resetSignature"
                >
                    Reset
                </button>
            </template>
            <template v-if="state === State.SelectType">
                <button
                    @click="confirmType"
                >
                    {{ typeChanged ? 'Confirm change' : 'Keep current' }}
                </button>
                <button
                    v-if="typeChanged"
                    @click="resetType"
                >
                    Reset
                </button>
            </template>
            <template v-if="state === State.SelectName">
                <button
                    :disabled="nameChanged && !isNameValid"
                    @click="confirmName"
                >
                    {{ nameChanged ? 'Confirm change' : 'Keep current' }}
                </button>
                <button
                    v-if="nameChanged"
                    @click="resetName"
                >
                    Reset
                </button>
            </template>
        </div>
        <div class="button-row">
            <button
                v-if="state > State.SelectSignature"
                @click="backButton"
            >
                Back
            </button>
            <button @click="cancel">
                Cancel
            </button>
            <button
                @click="deleteProperty"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.objex-ids-display {
    margin-left: -6px;
}
</style>

