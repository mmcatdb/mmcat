<script lang="ts">
import { defineComponent } from 'vue';
import { Cardinality, type CardinalitySettings } from '@/types/schema';
import RadioInput from '@/components/RadioInput.vue';

enum CardinalityType {
    ZeroOne = 0,
    ZeroStar = 1,
    OneOne = 2,
    OneStar = 3
}

export default defineComponent({
    components: {
        RadioInput
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
            morphismCardinality: CardinalityType.OneOne,
            dualCardinality: CardinalityType.OneOne,
            innerValue: Object.assign({}, this.modelValue),
            CardinalityType
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
        updateMorphismCardinality(newValue: CardinalityType): void {
            this.morphismCardinality = newValue;

            const newInnerValue = Object.assign({}, this.innerValue);
            newInnerValue.domCodMin = newValue < 2 ? Cardinality.Zero : Cardinality.One;
            newInnerValue.domCodMax = newValue % 2 === 0 ? Cardinality.One : Cardinality.Star;

            this.innerValue = newInnerValue;
            this.updateInnerValue();
        },
        updateDualCardinality(newValue: CardinalityType): void {
            this.dualCardinality = newValue;

            const newInnerValue = Object.assign({}, this.innerValue);
            newInnerValue.codDomMin = newValue < 2 ? Cardinality.Zero : Cardinality.One;
            newInnerValue.codDomMax = newValue % 2 === 0 ? Cardinality.One : Cardinality.Star;

            this.innerValue = newInnerValue;
            this.updateInnerValue();
        },
        updateInnerValue() {
            this.$emit('update:modelValue', this.innerValue);
        }
    }
});
</script>

<template>
    <tr>
        <td
            class="label"
            rowspan="2"
        >
            Cardinality:
        </td>
        <td class="value">
            <RadioInput
                :model-value="morphismCardinality"
                :value="CardinalityType.ZeroOne"
                @update:model-value="updateMorphismCardinality"
            >
                0..1
            </RadioInput>
            <RadioInput
                :model-value="morphismCardinality"
                :value="CardinalityType.ZeroStar"
                @update:model-value="updateMorphismCardinality"
            >
                0..*
            </RadioInput>
        </td>
    </tr>
    <tr>
        <td class="value">
            <RadioInput
                :model-value="morphismCardinality"
                :value="CardinalityType.OneOne"
                @update:model-value="updateMorphismCardinality"
            >
                1..1
            </RadioInput>
            <RadioInput
                :model-value="morphismCardinality"
                :value="CardinalityType.OneStar"
                @update:model-value="updateMorphismCardinality"
            >
                1..*
            </RadioInput>
        </td>
    </tr>
    <tr>
        <td
            class="label"
            rowspan="2"
        >
            Dual cardinality:
        </td>
        <td class="value">
            <RadioInput
                :model-value="dualCardinality"
                :value="CardinalityType.ZeroOne"
                @update:model-value="updateDualCardinality"
            >
                0..1
            </RadioInput>
            <RadioInput
                :model-value="dualCardinality"
                :value="CardinalityType.ZeroStar"
                @update:model-value="updateDualCardinality"
            >
                0..*
            </RadioInput>
        </td>
    </tr>
    <tr>
        <td class="value">
            <RadioInput
                :model-value="dualCardinality"
                :value="CardinalityType.OneOne"
                @update:model-value="updateDualCardinality"
            >
                1..1
            </RadioInput>
            <RadioInput
                :model-value="dualCardinality"
                :value="CardinalityType.OneStar"
                @update:model-value="updateDualCardinality"
            >
                1..*
            </RadioInput>
        </td>
    </tr>
</template>

<style scoped>

</style>

