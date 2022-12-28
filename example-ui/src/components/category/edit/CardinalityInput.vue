<script lang="ts">
import { defineComponent } from 'vue';
import { Cardinality, compareCardinalitySettings, type CardinalitySettings, type Max, type Min } from '@/types/schema';
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
        },
        disabled: {
            type: Boolean,
            required: false,
            default: false
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            morphismCardinality: this.getCardinalityTypeFromMinMax(this.modelValue.domCodMin, this.modelValue.domCodMax),
            dualCardinality: this.getCardinalityTypeFromMinMax(this.modelValue.codDomMin, this.modelValue.codDomMax),
            innerValue: Object.assign({}, this.modelValue),
            CardinalityType
        };
    },
    watch: {
        modelValue: {
            handler(newValue: CardinalitySettings): void {
                if (!compareCardinalitySettings(newValue, this.innerValue)) {
                    this.innerValue = Object.assign({}, this.modelValue);
                    this.morphismCardinality = this.getCardinalityTypeFromMinMax(newValue.domCodMin, newValue.domCodMax);
                    this.dualCardinality = this.getCardinalityTypeFromMinMax(newValue.codDomMin, newValue.codDomMax);
                }
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
        },
        getCardinalityTypeFromMinMax(min: Min, max: Max): CardinalityType {
            return (min === Cardinality.Zero ? 0 : 1) * 2 + (max === Cardinality.One ? 0 : 1);
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
                :disabled="disabled"
                @update:model-value="updateMorphismCardinality"
            >
                0..1
            </RadioInput>
            <RadioInput
                :model-value="morphismCardinality"
                :value="CardinalityType.ZeroStar"
                :disabled="disabled"
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
                :disabled="disabled"
                @update:model-value="updateMorphismCardinality"
            >
                1..1
            </RadioInput>
            <RadioInput
                :model-value="morphismCardinality"
                :value="CardinalityType.OneStar"
                :disabled="disabled"
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
                :disabled="disabled"
                @update:model-value="updateDualCardinality"
            >
                0..1
            </RadioInput>
            <RadioInput
                :model-value="dualCardinality"
                :value="CardinalityType.ZeroStar"
                :disabled="disabled"
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
                :disabled="disabled"
                @update:model-value="updateDualCardinality"
            >
                1..1
            </RadioInput>
            <RadioInput
                :model-value="dualCardinality"
                :value="CardinalityType.OneStar"
                :disabled="disabled"
                @update:model-value="updateDualCardinality"
            >
                1..*
            </RadioInput>
        </td>
    </tr>
</template>

<style scoped>
.value button:first-of-type {
    margin-right: 4px;
}
</style>

