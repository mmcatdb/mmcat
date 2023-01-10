<script setup lang="ts">
import { GraphSimpleProperty, GraphComplexProperty, SequenceSignature, type GraphParentProperty } from '@/types/accessPath/graph';
import { PropertyType, type Graph, createDefaultFilter, Node } from '@/types/categoryGraph';
import { StaticName, type Name } from '@/types/identifiers';
import { ref, computed } from 'vue';
import SignatureInput from '../input/SignatureInput.vue';
import TypeInput from '../input/TypeInput.vue';
import NameInput from '../input/NameInput.vue';
import type { DatabaseWithConfiguration } from '@/types/database';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

enum State {
    SelectSignature,
    SelectType,
    SelectName
}

type AddPropertyProps = {
    graph: Graph;
    database: DatabaseWithConfiguration;
    parentProperty: GraphParentProperty;
}

const props = defineProps<AddPropertyProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const type = ref(PropertyType.Simple);
const signature = ref(SequenceSignature.null(props.parentProperty.node));
const name = ref<Name>(StaticName.fromString(''));
const state = ref(State.SelectSignature);
const filter = ref(createDefaultFilter(props.database.configuration));
const typeIsDetermined = ref(false);

const nameIsValid = computed(() => !(name.value instanceof StaticName) || !!name.value.value);

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

function confirmSignature() {
    const node = signature.value.sequence.lastNode;
    name.value = StaticName.fromString(node.schemaObject.label.toLowerCase());
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
    if (!props.database.configuration.isComplexPropertyAllowed)
        return PropertyType.Simple;

    // Auxiliary property.
    if (signature.value.isNull)
        return PropertyType.Complex;

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
</script>

<template>
    <div class="outer">
        <h2>Add property</h2>
        <ValueContainer>
            <ValueRow
                v-if="state >= State.SelectSignature"
                label="Signature:"
            >
                {{ signature }}
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
                    :graph="graph"
                    :database="database"
                    :root-node="parentProperty.node"
                />
            </ValueRow>
        </ValueContainer>
        <div
            v-if="state === State.SelectSignature"
            class="button-row"
        >
            <SignatureInput
                v-model="signature"
                :graph="graph"
                :filter="filter"
                :default-is-null="true"
            >
                <template #nullButton>
                    Auxiliary property
                </template>
            </SignatureInput>
        </div>
        <div class="button-row">
            <button
                :disabled="(state === State.SelectSignature && !database.configuration.isGrouppingAllowed && signature.isNull) ||
                    (state === State.SelectName && !nameIsValid)"
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
