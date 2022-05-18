<script lang="ts">
import type { SchemaId } from '@/types/identifiers';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        schemaId: {
            type: Object as () => SchemaId,
            required: true
        }
    }
});
</script>

<template>
    <span>
        <template
            v-for="(signature, signatureIndex) in schemaId.signatures"
            :key="signatureIndex"
        >
            <span class="signature-span">
                {{ signature }}
            </span>
            <span
                v-if="signatureIndex < schemaId.signatures.length - 1"
                class="comma-span"
            >,</span>
        </template>
    </span>
</template>

<style scoped>
.signature-span {
    background-color: var(--vt-c-black-mute);
    border-radius: 4px;
    padding: 0px 6px 0px 4px;
}

.comma-span {
    margin-right: 8px;
    margin-left: 2px;
}
</style>
