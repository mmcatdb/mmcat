<script setup lang="ts">
withDefaults(defineProps<{ modelValue: string}>(), {
    modelValue: '',
});

const emit = defineEmits([ 'update:modelValue' ]);
</script>

<template>
    <div>
        <h2>Access path json editor</h2>
        <br />
        <textarea
            :value="modelValue"
            class="access-path-input"
            spellcheck="false"
            @input="(event) => emit('update:modelValue', (event.target as HTMLInputElement).value)"
        />
    </div>
</template>

<style scoped>
.access-path-input {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}
</style>
