<script setup lang="ts">
import { SequenceSignature } from '@/types/accessPath/graph';
import { type Graph, type Node, createDefaultFilter } from '@/types/categoryGraph';
import type { DatabaseWithConfiguration } from '@/types/database';
import { DynamicName, Signature, StaticName, type Name } from '@/types/identifiers';
import { ref, watch } from 'vue';
import StaticNameInput from './StaticNameInput.vue';
import SignatureInput from './SignatureInput.vue';

enum NameType {
    Static,
    Dynamic,
    Anonymous
}

type NameInputProps = {
    graph: Graph;
    database: DatabaseWithConfiguration;
    rootNode: Node;
    modelValue: Name;
    isSelfIdentifier: boolean;
    disabled?: boolean;
};

const props = withDefaults(defineProps<NameInputProps>(), {
    disabled: false
});

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref(props.modelValue);
const type = ref(getNameType(props.modelValue));
const staticValue = ref(props.modelValue instanceof StaticName && !props.modelValue.isAnonymous ? props.modelValue : StaticName.fromString(''));
const dynamicValue = ref(SequenceSignature.fromSignature(props.modelValue instanceof DynamicName ? props.modelValue.signature : Signature.empty, props.rootNode));
const filter = ref(createDefaultFilter(props.database.configuration));

watch(() => props.modelValue, (newValue: Name) => {
    if (!newValue.equals(innerValue.value)) {
        innerValue.value = props.modelValue;
        type.value = getNameType(props.modelValue);
        staticValue.value = props.modelValue instanceof StaticName && !props.modelValue.isAnonymous ? props.modelValue : StaticName.fromString('');
        dynamicValue.value = SequenceSignature.fromSignature(props.modelValue instanceof DynamicName ? props.modelValue.signature : Signature.empty, props.rootNode);
    }
});

function getNameType(name: Name): NameType {
    return name instanceof StaticName
        ? (name.isAnonymous ? NameType.Anonymous : NameType.Static)
        : NameType.Dynamic;
}

function updateInnerValue() {
    switch (type.value) {
    case NameType.Static:
        innerValue.value = staticValue.value;
        break;
    case NameType.Dynamic:
        innerValue.value = DynamicName.fromSignature(dynamicValue.value.toSignature());
        break;
    case NameType.Anonymous:
        innerValue.value = StaticName.anonymous;
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
        :disabled="disabled || !database.configuration.isDynamicNamingAllowed || isSelfIdentifier"
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
    <input
        id="anonymous"
        v-model="type"
        type="radio"
        :value="NameType.Anonymous"
        :disabled="disabled || !database.configuration.isAnonymousNamingAllowed || isSelfIdentifier"
        @change="updateInnerValue"
    />
    <label
        for="anonymous"
        :class="{ value: type === NameType.Anonymous }"
    >
        Anonymous
    </label>
    <br />
    <div v-if="type === NameType.Dynamic">
        <SignatureInput
            v-model="dynamicValue"
            :graph="graph"
            :filter="filter"
            :disabled="disabled"
            @input="updateInnerValue"
        />
    </div>
</template>
