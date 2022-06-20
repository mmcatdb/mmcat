<script lang="ts">
import { defineComponent } from 'vue';
import { PropertyType } from '@/types/categoryGraph';

export default defineComponent({
    components: {

    },
    props: {
        modelValue: {
            type: String as () => PropertyType,
            required: true
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: this.modelValue,
            PropertyType
        };
    },
    watch: {
        modelValue: {
            handler(newValue: PropertyType): void {
                if (newValue !== this.innerValue)
                    this.innerValue = newValue;
            }
        }
    },
    methods: {
        updateInnerValue() {
            this.$emit('update:modelValue', this.innerValue);
        }
    }
});
</script>

<template>
    <input
        id="simple"
        v-model="innerValue"
        type="radio"
        :value="PropertyType.Simple"
        @change="updateInnerValue"
    />
    <label
        :class="{ value: innerValue === PropertyType.Simple }"
        for="simple"
    >
        Simple
    </label><br />
    <input
        id="complex"
        v-model="innerValue"
        type="radio"
        :value="PropertyType.Complex"
        @change="updateInnerValue"
    />
    <label
        :class="{ value: innerValue === PropertyType.Complex }"
        for="complex"
    >
        Complex
    </label>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

