<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        modelValue: {
            type: String,
            default: ''
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: ''
        };
    }
});
</script>

<template>
    <div>
        <h2>Access path json editor</h2>
        <br />
        <textarea
            :value="modelValue"
            class="accessPathInput"
            spellcheck="false"
            @input="(event) => $emit('update:modelValue', (event.target as HTMLInputElement).value)"
        />
    </div>
</template>

<style scoped>
.accessPathInput {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}
</style>
