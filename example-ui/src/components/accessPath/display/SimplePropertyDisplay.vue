<script setup lang="ts">
import type { GraphSimpleProperty } from '@/types/accessPath/graph';
import type { SimpleProperty } from '@/types/accessPath/basic';
import SignatureDisplay from '@/components/category/SignatureDisplay.vue';
import NameDisplay from '@/components/category/NameDisplay.vue';

type SimplePropertyDisplayProps = {
    property: GraphSimpleProperty | SimpleProperty;
    isLast: boolean;
};

defineProps<SimplePropertyDisplayProps>();

const emit = defineEmits([ 'simple:click' ]);
</script>

<template>
    <span
        class="name-text"
        @click="emit('simple:click', property)"
    >
        <NameDisplay :name="property.name" />: <SignatureDisplay :signature="property.signature" />{{ isLast ? '' : ',' }}
    </span>
</template>

<style scoped>
.name-text:hover {
    background-color: black;
}

.name-text {
    width: fit-content;
    padding: 2px 4px;
    border-radius: 4px;
}
</style>
