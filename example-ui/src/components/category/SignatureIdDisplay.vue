<script setup lang="ts">
import type { SignatureId } from '@/types/identifiers';

type SignatureIdDisplayProps = {
    signatureId: SignatureId;
};

defineProps<SignatureIdDisplayProps>();
</script>

<template>
    <span>
        <template
            v-for="(signature, signatureIndex) in signatureId.signatures"
            :key="signatureIndex"
        >
            <span class="signature-span">
                {{ signature }}
            </span>
            <span
                v-if="signatureIndex < signatureId.signatures.length - 1"
                class="comma-span"
            >,</span>
        </template>
    </span>
</template>

<style scoped>
.comma-span {
    margin-right: 8px;
    margin-left: 2px;
}
</style>
