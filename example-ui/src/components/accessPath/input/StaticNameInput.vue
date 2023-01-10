<script setup lang="ts">
import { StaticName } from '@/types/identifiers';
import { ref, watch } from 'vue';

type StaticNameInputProps = {
    modelValue: StaticName;
    disabled?: boolean;
}

const props = withDefaults(defineProps<StaticNameInputProps>(), {
    disabled: false
});

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref<StaticName>();
const staticValue = ref<string>('');

setValueFromParent(props.modelValue);

watch(() => props.modelValue, setValueFromParent);

function setValueFromParent(newValue: StaticName) {
    if (newValue.equals(innerValue.value))
        return;

    staticValue.value = validateDatabaseName(props.modelValue.value);
    if (staticValue.value !== props.modelValue.value) {
        innerValue.value = StaticName.fromString(staticValue.value);
        emit('update:modelValue', innerValue.value);
        return;
    }
    innerValue.value = props.modelValue;
}

function validateDatabaseName(value: string): string {
    return value.replace(/\s/g, '_').replace(/[^\w.]/g, '.');
}

function updateInnerValue() {
    staticValue.value = validateDatabaseName(staticValue.value);
    innerValue.value = StaticName.fromString(staticValue.value);
    emit('update:modelValue', innerValue.value);
}
</script>

<template>
    <input
        v-model="staticValue"
        :disabled="disabled"
        @input="updateInnerValue"
    />
</template>
