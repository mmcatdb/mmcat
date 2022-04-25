<script lang="ts">
import { defineComponent } from 'vue';
import { Cardinality, type CardinalitySettings } from '@/types/schema';

export default defineComponent({
    components: {

    },
    props: {
        modelValue: {
            type: Object as () => CardinalitySettings,
            required: true
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: Object.assign({}, this.modelValue),
            Cardinality
        };
    },
    watch: {
        modelValue: {
            handler(newValue: CardinalitySettings): void {
                if (
                    newValue.domCodMin !== this.innerValue.domCodMin ||
                    newValue.domCodMax !== this.innerValue.domCodMax ||
                    newValue.codDomMin !== this.innerValue.codDomMin ||
                    newValue.codDomMax !== this.innerValue.codDomMax
                )
                    this.innerValue = Object.assign({}, this.modelValue);
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
    <div class="outer">
        <span>
            (Dom -> Cod) min:
            <input
                v-model="innerValue.domCodMin"
                type="radio"
                :value="Cardinality.Zero"
            />
            0
            <input
                v-model="innerValue.domCodMin"
                type="radio"
                :value="Cardinality.One"
            />
            1
        </span>
        <br />
        <span>
            (Dom -> Cod) max:
            <input
                v-model="innerValue.domCodMax"
                type="radio"
                :value="Cardinality.One"
            />
            1
            <input
                v-model="innerValue.domCodMax"
                type="radio"
                :value="Cardinality.Star"
            />
            *
        </span>
        <br />
        <span>
            (Cod -> Dom) min:
            <input
                v-model="innerValue.codDomMin"
                type="radio"
                :value="Cardinality.Zero"
            />
            0
            <input
                v-model="innerValue.codDomMin"
                type="radio"
                :value="Cardinality.One"
            />
            1
        </span>
        <br />
        <span>
            (Cod -> Dom) max:
            <input
                v-model="innerValue.codDomMax"
                type="radio"
                :value="Cardinality.One"
            />
            1
            <input
                v-model="innerValue.codDomMax"
                type="radio"
                :value="Cardinality.Star"
            />
            *
        </span>
    </div>
</template>

<style scoped>
.outer {
    background-color: darkgreen;
    padding: 12px;
}
</style>

