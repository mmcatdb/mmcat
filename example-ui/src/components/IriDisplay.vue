<script setup lang="ts">
import type { Iri } from '@/types/integration';
import { computed } from 'vue';

interface IriDisplayProps {
    iri?: Iri;
    maxChars?: number;
    clickable?: boolean;
}

const props = defineProps<IriDisplayProps>();

const iriRows = computed(() => {
    if (!props.iri || !props.maxChars || props.iri.length <= props.maxChars)
        return [ props.iri ];

    const split = props.iri.split('/');
    const output = [] as string[];

    const maxSectionLength = split.reduce((total, section) => Math.max(total, section.length), 0);
    // If the longest section is already longer than the given maxChars, so be it.
    // Minus one for the '/' at the end.
    const maxRowLength = Math.max(props.maxChars, maxSectionLength) - 1;

    let firstSection = split.shift();
    if (!firstSection)
        return [ props.iri ];
    let currentRow = firstSection;

    split.forEach(section => {
        if (currentRow.length + section.length <= maxRowLength) {
            currentRow += '/' + section;
        }
        else {
            output.push(currentRow + '/');
            currentRow = section;
        }
    });

    output.push(currentRow);

    return output;
});

function click() {
    if (!props.clickable)
        return;

    window.open(props.iri, '_blank', 'noreferrer');
}
</script>

<template>
    <div class="outer">
        <div
            v-for="row in iriRows"
            :key="row"
            class="text-bold"
            :class="{ clickable: props.clickable }"
            @click="click"
        >
            {{ row }}
        </div>
    </div>
</template>

<style scoped>
.outer {
    display: flex;
    flex-direction: column;
}
</style>
