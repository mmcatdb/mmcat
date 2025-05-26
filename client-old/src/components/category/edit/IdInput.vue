<script setup lang="ts">
import type { Node } from '@/types/categoryGraph';
import { ObjexIds, SignatureId, Type } from '@/types/identifiers';
import { shallowRef, watch } from 'vue';
import AddSimpleId from './AddSimpleId.vue';
import AddComplexId from './AddComplexId.vue';

/*
 * When the id is simple (it has exactly one signature) the corresponding morphism must have cardinality 1:1.
 * When the id is complex, all its morphisms have to have cardinality n:1 (because otherwise they would be simple identifiers so the complex one wouldn't be needed).
 * The last option is a either a Value or a Generated one.
 */

enum State {
    SelectType,
    Simple,
    Complex
}

type IdInputProps = {
    node: Node;
    modelValue: ObjexIds | undefined;
};

const props = defineProps<IdInputProps>();
const innerValue = shallowRef(props.modelValue);

watch(() => props.modelValue, (newValue: ObjexIds | undefined) => {
    if (innerValue.value !== newValue)
        innerValue.value = newValue;
});

const state = shallowRef(State.SelectType);

const emit = defineEmits([ 'save', 'cancel', 'update:modelValue' ]);

function addSignatureId(signatureId: SignatureId) {
    const currentIds = innerValue.value?.signatureIds ?? [];
    innerValue.value = ObjexIds.createSignatures([ ...currentIds, signatureId ]);

    emit('update:modelValue', innerValue.value);
}

function cancel() {
    emit('cancel');
}

function selectSimple() {
    state.value = State.Simple;
}

function selectComplex() {
    state.value = State.Complex;
}

function selectValue() {
    innerValue.value = ObjexIds.createNonSignatures(Type.Value);
    emit('update:modelValue', innerValue.value);
}

function selectGenerated() {
    innerValue.value = ObjexIds.createNonSignatures(Type.Generated);
    emit('update:modelValue', innerValue.value);
}
</script>

<template>
    <template v-if="state === State.SelectType">
        <h2>{{ node.schemaObjex.ids ? 'Add Id' : 'Create Id' }}</h2>
        <div class="button-row">
            <button @click="selectSimple">
                Simple
            </button>
            <button @click="selectComplex">
                Complex
            </button>
            <template v-if="!node.schemaObjex.ids">
                <button @click="selectValue">
                    Value
                </button>
                <button @click="selectGenerated">
                    Generated
                </button>
            </template>
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </template>
    <template v-else-if="state === State.Simple">
        <AddSimpleId
            :node="node"
            @save="addSignatureId"
            @cancel="cancel"
        />
    </template>
    <template v-else-if="state === State.Complex">
        <AddComplexId
            :node="node"
            @save="addSignatureId"
            @cancel="cancel"
        />
    </template>
</template>
