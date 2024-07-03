<script setup lang="ts">
import type { SequenceSignature } from '@/types/accessPath/graph';
import type { Signature } from '@/types/identifiers';

type SignatureDisplayProps = {
    signature: Signature | SequenceSignature;
};

defineProps<SignatureDisplayProps>();
</script>

<template>
    <span class="monospace-font">{{ signature.toString() }}</span>
</template>
