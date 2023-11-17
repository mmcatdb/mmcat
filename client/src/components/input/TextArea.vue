<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';

type TextAreaProps = {
    modelValue?: string;
    minRows?: number;
    class?: string;
    readonly?: boolean;
};

const props = withDefaults(defineProps<TextAreaProps>(), {
    modelValue: undefined,
    minRows: 6,
    class: '',
});
const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref(props.modelValue);
const textArea = ref<HTMLTextAreaElement>();

watch(() => props.modelValue, (newValue?: string) => {
    if (newValue === innerValue.value)
        return;

    innerValue.value = newValue;
    resize();
});

function handleInput(event: Event) {
    const newValue = (event.target as HTMLInputElement).value;
    if (newValue === innerValue.value)
        return;

    innerValue.value = newValue;
    emit('update:modelValue', innerValue.value);
    resize();
}

onMounted(() => resize());

function resize() {
    const minHeight = 24 * props.minRows + 18; // Line height should be 24 px, padding 8 + 8 px and border 1 + 1 px.
    if (!textArea.value)
        return;

    const clone = textArea.value.cloneNode() as HTMLTextAreaElement;
    document.body.appendChild(clone);
    clone.style.width = textArea.value.offsetWidth + 'px';
    clone.style.height = '0px';
    const trueHeight = clone.scrollHeight + 2; // Add 2 pixels for the borders.
    const newHeight = trueHeight > minHeight ? trueHeight : minHeight;
    clone.remove();

    textArea.value.style.height = newHeight + 'px';
}
</script>

<template>
    <textarea
        ref="textArea"
        :value="innerValue"
        spellcheck="false"
        class="textarea-dark"
        :class="props.class"
        :readonly="props.readonly"
        @input="handleInput"
    />
</template>
