<script lang="ts">
import type { SchemaObject } from '@/types/schema';
import { defineComponent } from 'vue';
import SchemaId from './SchemaId.vue';

export default defineComponent({
    components: { SchemaId },
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
        <SchemaId
            v-for="(schemaId, idIndex) in schemaObject.schemaIds"
            :key="idIndex"
            :schema-id="schemaId"
        />
    </div>
</template>

<style scoped>
.outer {
    display: flex;
    flex-direction: column;
}
</style>
