<script setup lang="ts">
import { type Name, DynamicName } from '@/types/identifiers';
import SignatureDisplay from './SignatureDisplay.vue';

type NameDisplayProps = {
    name: Name;
};

defineProps<NameDisplayProps>();
</script>

<template>
    <template v-if="name instanceof DynamicName">
        {{ '<' }}<SignatureDisplay :signature="name.signature" />{{ '>' }}
    </template>
    <template v-else>
        {{ name }}
    </template>
</template>
