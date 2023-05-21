<script setup lang="ts">
import type { Node } from '@/types/categoryGraph';
import { Type } from '@/types/identifiers';
import { ref } from 'vue';
import AddSimpleId from './AddSimpleId.vue';
import AddComplexId from './AddComplexId.vue';
import { useEvocat } from '@/utils/injects';

const { evocat } = $(useEvocat());

/*
 * When the id is simple (it has exactly one signature) the corresponding morphism must have cardinality 1:1.
 * When the id is complex, all its morphisms have to have cardinality n:1 (because otherwise they would be simple identifiers so the complex one wouldn't be needed).
 * The last option is a either a Value or a Generated one.
 */

enum State {
    SelectType,
    Simple,
    Complex
}

type AddIdProps = {
    node: Node;
};

const props = defineProps<AddIdProps>();

const state = ref(State.SelectType);

const emit = defineEmits([ 'save', 'cancel' ]);

function save() {
    emit('save');
}

function cancel() {
    emit('cancel');
}

function selectSimple() {
    state.value = State.Simple;
}

function selectComplex() {
    state.value = State.Complex;
}

function selectValue() {
    evocat.createId(props.node.schemaObject, { type: Type.Value });
    emit('save');
}

function selectGenerated() {
    evocat.createId(props.node.schemaObject, { type: Type.Generated });
    emit('save');
}
</script>

<template>
    <template v-if="state === State.SelectType">
        <h2>{{ node.schemaObject.ids ? 'Add Id' : 'Create Id' }}</h2>
        <div class="button-row">
            <button @click="selectSimple">
                Simple
            </button>
            <button @click="selectComplex">
                Complex
            </button>
            <template v-if="!node.schemaObject.ids">
                <button @click="selectValue">
                    Value
                </button>
                <button @click="selectGenerated">
                    Generated
                </button>
            </template>
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </template>
    <template v-else-if="state === State.Simple">
        <AddSimpleId
            :node="node"
            @save="save"
            @cancel="cancel"
        />
    </template>
    <template v-else-if="state === State.Complex">
        <AddComplexId
            :node="node"
            @save="save"
            @cancel="cancel"
        />
    </template>
</template>
