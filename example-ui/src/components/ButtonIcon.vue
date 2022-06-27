<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        disabled: {
            type: Boolean,
            required: false,
            default: false
        }
    },
    emits: [ 'click' ],
    data() {
        return {

        };
    },
    methods: {
        onClick() {
            if (!this.disabled)
                this.$emit('click');
        }
    }
});
</script>

<template>
    <span
        class="button-icon"
        :class="{ 'icon-disabled': disabled }"
        @click="onClick"
    >
        <slot />
    </span>
</template>

<style scoped>
.icon-disabled {
    color: var(--color-text);
    cursor: auto;
}
</style>
