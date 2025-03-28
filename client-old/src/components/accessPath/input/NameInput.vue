<script setup lang="ts">
import { SequenceSignature } from '@/types/accessPath/graph';
import { type Node, createDefaultFilter } from '@/types/categoryGraph';
import type { Datasource } from '@/types/datasource';
import { DynamicName, Signature, StaticName, type Name } from '@/types/identifiers';
import { ref, shallowRef, watch } from 'vue';
import StaticNameInput from './StaticNameInput.vue';
import SignatureInput from './SignatureInput.vue';

enum NameType {
    Static,
    Dynamic,
}

type NameInputProps = {
    datasource: Datasource;
    rootNode: Node;
    modelValue: Name;
    isSelfIdentifier: boolean;
    disabled?: boolean;
};

const props = withDefaults(defineProps<NameInputProps>(), {
    disabled: false,
});

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = shallowRef(props.modelValue);
const type = ref(getNameType(props.modelValue));
const staticValue = ref(props.modelValue instanceof StaticName ? props.modelValue : new StaticName(''));
const dynamicValue = shallowRef(SequenceSignature.fromSignature(props.modelValue instanceof DynamicName ? props.modelValue.signature : Signature.empty, props.rootNode));
const filter = ref(createDefaultFilter(props.datasource.configuration));

watch(() => props.modelValue, (newValue: Name) => {
    if (!newValue.equals(innerValue.value)) {
        innerValue.value = props.modelValue;
        type.value = getNameType(props.modelValue);
        staticValue.value = props.modelValue instanceof StaticName ? props.modelValue : new StaticName('');
        dynamicValue.value = SequenceSignature.fromSignature(props.modelValue instanceof DynamicName ? props.modelValue.signature : Signature.empty, props.rootNode);
    }
});

function getNameType(name: Name): NameType {
    if (name instanceof DynamicName)
        return NameType.Dynamic;

    return NameType.Static;
}

function updateInnerValue() {
    switch (type.value) {
    case NameType.Static:
        innerValue.value = staticValue.value;
        break;
    case NameType.Dynamic:
        innerValue.value = DynamicName.fromSignature(dynamicValue.value.toSignature());
        break;
    }

    emit('update:modelValue', innerValue.value);
}
</script>

<template>
    <input
        id="static"
        v-model="type"
        type="radio"
        :value="NameType.Static"
        :disabled="disabled"
        @change="updateInnerValue"
    />
    <label
        for="static"
        :class="{ value: type === NameType.Static }"
    >
        Static
    </label>
    <br />
    <StaticNameInput
        v-model="staticValue"
        :disabled="type !== NameType.Static"
        @update:model-value="updateInnerValue"
    />
    <br />
    <input
        id="dynamic"
        v-model="type"
        type="radio"
        :value="NameType.Dynamic"
        :disabled="disabled || isSelfIdentifier"
        @change="updateInnerValue"
    />
    <label
        for="dynamic"
        :class="{ value: type === NameType.Dynamic }"
    >
        Dynamic
    </label>
    <br />
    <span :class="{ disabled: type !== NameType.Dynamic }">
        {{ dynamicValue }}
    </span>
    <br />
    <div v-if="type === NameType.Dynamic">
        <SignatureInput
            v-model="dynamicValue"
            :filter="filter"
            :disabled="disabled"
            @input="updateInnerValue"
        />
    </div>
</template>
