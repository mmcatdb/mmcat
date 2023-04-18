<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { SelectionType, type Graph, type Node } from '@/types/categoryGraph';
import { Cardinality, type Min } from '@/types/schema';
import MinimumInput from './MinimumInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { computed } from '@vue/reactivity';

type AddPropertyProps = {
    graph: Graph;
};

const props = defineProps<AddPropertyProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const label = ref('');
const keyIsValid = ref(true);
const node = ref<Node | null>(null);
const min = ref<Min>(Cardinality.One);

const nodeSelected = computed(() => !!node.value);

onMounted(() => props.graph.addNodeListener('tap', onNodeTapHandler));

onUnmounted(() => {
    props.graph.removeNodeListener('tap', onNodeTapHandler);
    node.value?.unselect();
});

function onNodeTapHandler(tappedNode: Node): void {
    node.value?.unselect();

    node.value = tappedNode.equals(node.value) ? null : tappedNode;

    node.value?.select({ type: SelectionType.Selected, level: 0 });
}

function save() {
    if (!node.value)
        return;

    const object = props.graph.schemaCategory.createObject(label.value);
    props.graph.createNode(object, 'new');

    const morphism = props.graph.schemaCategory.createMorphism(node.value.schemaObject, object, min.value, '');
    props.graph.createEdge(morphism, 'new');

    props.graph.layout();
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
                {{ node?.schemaObject.label }}
            </ValueRow>
            <MinimumInput
                v-model="min"
            />
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!keyIsValid || !label || !nodeSelected"
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
