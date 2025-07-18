<script setup lang="ts">
import { ref } from 'vue';
import { SelectionType, type Node } from '@/types/categoryGraph';
import { Cardinality, type Min } from '@/types/schema';
import MinimumInput from './MinimumInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SingleNodeInput from '@/components/input/SingleNodeInput.vue';
import { ObjexIds, Type } from '@/types/identifiers';
import { useEvocat } from '@/utils/injects';

const { evocat, graph } = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const label = ref('');
const keyIsValid = ref(true);
const node = ref<Node>();
const min = ref<Min>(Cardinality.One);

function save() {
    evocat.compositeOperation('addProperty', () => {
        if (!node.value)
            return;

        const objex = evocat.createObjex({
            label: label.value,
            ids: ObjexIds.createNonSignatures(Type.Value),
        });

        evocat.createMorphism({
            domKey: node.value.schemaObjex.key,
            codKey: objex.key,
            min: min.value,
        });
    });

    graph.layout();
    emit('save');
}

function cancel() {
    emit('cancel');
}
</script>

<template>
    <div>
        <h2>Add Property</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input v-model="label" />
            </ValueRow>
            <ValueRow label="Parent object:">
                <SingleNodeInput
                    v-model="node"
                    :type="SelectionType.Root"
                />
            </ValueRow>
            <MinimumInput
                v-model="min"
            />
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!keyIsValid || !label || !node"
                @click="save"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>
