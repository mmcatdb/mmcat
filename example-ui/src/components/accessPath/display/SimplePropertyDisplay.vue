<script lang="ts">
import type { SimpleProperty } from '@/types/accessPath';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        property: {
            type: Object as () => SimpleProperty,
            required: true
        },
        isLast: {
            type: Boolean,
            required: true
        }
    },
    emits: [ 'simple:click' ]
});
</script>

<template>
    <span @click="$emit('simple:click', property)">
        {{ property.name }}: {{ property.signature }}{{ isLast ? '' : ',' }}
    </span>
</template>

<style scoped>

</style>
