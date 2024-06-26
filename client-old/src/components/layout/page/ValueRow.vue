<script setup lang="ts">
import { useSlots } from 'vue';

type ValueRowProps = {
    label?: string;
};

defineProps<ValueRowProps>();
const slots = useSlots();
</script>

<template>
    <tr>
        <td class="label">
            <slot name="label" />
            <template v-if="!slots.label">
                {{ label }}
            </template>
        </td>
        <td class="value">
            <slot />
        </td>
    </tr>
</template>

<style scoped>

</style>
