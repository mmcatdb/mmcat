<script lang="ts">
import { StaticName } from '@/types/identifiers';
import { defineComponent } from 'vue';

export default defineComponent({
    components: {

    },
    props: {
        modelValue: {
            type: Object as () => StaticName,
            required: true
        },
        disabled: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: this.modelValue,
            staticValue: this.modelValue.value
        };
    },
    watch: {
        modelValue: {
            handler(newValue: StaticName): void {
                if (!newValue.equals(this.innerValue)) {
                    this.innerValue = this.modelValue;
                    this.staticValue = this.modelValue.value;
                }
            }
        }
    },
    methods: {
        updateInnerValue() {
            this.innerValue = StaticName.fromString(this.staticValue);
            this.$emit('update:modelValue', this.innerValue);
        }
    }
});
</script>

<template>
    <input
        v-model="staticValue"
        @input="updateInnerValue"
    />
</template>
