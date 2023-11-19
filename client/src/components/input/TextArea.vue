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
    history.length = 0;
    history.push(newValue);
    reverseHistory.length = 0;
    resize();
});

function handleInput(event: Event) {
    const newValue = (event.target as HTMLInputElement).value;
    if (newValue === innerValue.value)
        return;

    innerHandleInput(newValue);
    history.push(newValue);
    reverseHistory.length = 0;
}

function innerHandleInput(newValue: string | undefined) {
    innerValue.value = newValue;
    emit('update:modelValue', innerValue.value);
    resize();
}

const history = [ props.modelValue ];
const reverseHistory: string[] = [];

const TAB_KEY_VALUE = '    ';

function handleKeyDown(event: KeyboardEvent) {
    if (event.key === 'Tab') {
        event.preventDefault();
        const target = event.target as HTMLTextAreaElement;

        const startIndex = target.selectionStart;
        const endIndex = target.selectionEnd;

        const stringBefore = target.value.substring(0, startIndex);

        const lastNewLineIndex = stringBefore.lastIndexOf('\n');
        const lenghtToAdd = TAB_KEY_VALUE.length - (startIndex - lastNewLineIndex - 1) % TAB_KEY_VALUE.length;
        const stringToAdd = TAB_KEY_VALUE.substring(0, lenghtToAdd);

        target.value = target.value.substring(0, startIndex) + stringToAdd + target.value.substring(endIndex);
        target.selectionStart = target.selectionEnd = startIndex + lenghtToAdd;
    }
    else if (event.key === 'z' && event.ctrlKey) {
        event.preventDefault();
        if (history.length <= 1)
            return;

        const currentValue = history.pop();
        const newValue = history[history.length - 1];

        innerHandleInput(newValue);
        reverseHistory.push(currentValue as string);
    }
    else if (event.key === 'y' && event.ctrlKey) {
        event.preventDefault();
        const newValue = reverseHistory.pop();
        if (newValue === undefined)
            return;

        innerHandleInput(newValue);
        history.push(newValue);
    }
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
        @keydown="handleKeyDown"
    />
</template>
