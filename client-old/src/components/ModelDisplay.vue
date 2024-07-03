<script setup lang="ts">
import type { Model } from '@/types/model';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { onMounted, ref } from 'vue';

defineProps<{ model: Model }>();

const textArea = ref<HTMLTextAreaElement>();

onMounted(resizeTextArea);

function resizeTextArea() {
    if (!textArea.value)
        return;

    textArea.value.style.height = textArea.value.scrollHeight + 'px';
}
</script>

<template>
    <div class="model-display">
        <h2>{{ model.jobLabel }}</h2>
        <ValueContainer>
            <ValueRow label="Job id:">
                {{ model.jobId }}
            </ValueRow>
            <ValueRow label="Job label:">
                {{ model.jobLabel }}
            </ValueRow>
        </ValueContainer>
        <textarea
            ref="textArea"
            class="model-commands"
            spellcheck="false"
            :value="model.commands"
            :disabled="true"
            @input="resizeTextArea"
        />
    </div>
</template>

<style scoped>
.model-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 244px;
}

.model-commands {
    color: white;
    background-color: var(--color-background-dark);
    font-family: Consolas, monospace;
    min-width: 600px;
    font-size: 15px;
    overflow: hidden;
    resize: none;
    padding: 8px;
    border-radius: 8px;
}
</style>
