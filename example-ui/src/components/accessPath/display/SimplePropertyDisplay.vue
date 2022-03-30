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
    <span
        class="name-text"
        @click="$emit('simple:click', property)"
    >
        {{ property.name }}: {{ property.signature }}{{ isLast ? '' : ',' }}
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
