<script setup lang="ts">
import SignatureIdDisplay from '@/components/category/SignatureIdDisplay.vue';
import type { ObjexIds, SignatureId } from '@/types/identifiers';
import { ref, watch } from 'vue';

type PrimaryKeyInputProps = {
    ids: ObjexIds;
    modelValue: SignatureId;
};

const props = defineProps<PrimaryKeyInputProps>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref(props.modelValue);

/*
watch(() => props.modelValue, (newValue: SignatureId) => {
    if (newValue.equals(innerValue.value))
        return;

    staticValue.value = validateDatasourceName(props.modelValue.value);
    if (staticValue.value !== props.modelValue.value) {
        innerValue.value = SignatureId.fromString(staticValue.value);
        emit('update:modelValue', innerValue.value);
        return;
    }
    innerValue.value = props.modelValue;
});

function validateDatasourceName(value: string): string {
    return value.replace(/\s/g, '_').replace(/[^\w.]/g, '.');
}

function updateInnerValue() {
    staticValue.value = validateDatasourceName(staticValue.value);
    innerValue.value = SignatureId.fromString(staticValue.value);
    emit('update:modelValue', innerValue.value);
}
*/
</script>

<template>
    <SignatureIdDisplay
        v-if="ids.isSignatures && ids.signatureIds.length === 1"
        :signature-id="ids.signatureIds[0]"
    />
    <span
        v-else-if="!ids.isSignatures"
        class="signature-span"
    >
        {{ ids.type }}
    </span>
    <div v-else>
        <!--
        <RadioInput
            :model-value="morphismCardinality"
            :value="CardinalityType.ZeroOne"
            :disabled="disabled"
            @update:model-value="updateMorphismCardinality"
        >
            0..1
        </RadioInput>
        -->
        TODO
    </div>
</template>
