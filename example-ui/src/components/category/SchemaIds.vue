<script lang="ts">
import type { SchemaObject } from '@/types/schema';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        schemaObject: {
            type: Object as () => SchemaObject,
            required: true
        }
    }
});
</script>

<template>
    <div class="outer">
        <div
            v-for="(schemaId, idIndex) in schemaObject.schemaIds"
            :key="idIndex"
            class="innerRow"
        >
            <template
                v-for="(signature, signatureIndex) in schemaId.signatures"
                :key="signatureIndex"
            >
                <span class="signatureSpan">
                    {{ signature }}
                </span>
                <span
                    v-if="signatureIndex < schemaId.signatures.length - 1"
                    class="commaSpan"
                >
                    ,
                </span>
            </template>
        </div>
    </div>
</template>

<style scoped>
.outer {
    display: flex;
    flex-direction: column;
}

.innerRow {
    display: flex;
    flex-direction: row;
}

.signatureSpan {
    background-color: var(--vt-c-black-mute);
    border-radius: 4px;
    padding: 0px 6px 0px 4px;
}

.commaSpan {
    margin-right: 8px;
    margin-left: 2px;
}
</style>
