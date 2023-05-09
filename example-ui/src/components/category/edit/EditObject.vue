<script setup lang="ts">
import type { Node } from '@/types/categoryGraph';
import { computed, ref } from 'vue';
import ObjectIdsDisplay from '@/components/category/ObjectIdsDisplay.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import ButtonIcon from '@/components/ButtonIcon.vue';
import AddId from './AddId.vue';
import IriDisplay from '@/components/IriDisplay.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { useEvocat } from '@/utils/injects';

const evocat = $(useEvocat());

type EditObjectProps = {
    node: Node;
};

const props = defineProps<EditObjectProps>();

const emit = defineEmits([ 'save', 'cancel', 'update' ]);

const label = ref(props.node.schemaObject.label);
const addingId = ref(false);

const changed = computed(() => label.value !== props.node.schemaObject.label || addingId.value);
const isNew = computed(() => props.node.schemaObject.isNew);

defineExpose({ changed });

function save() {
    props.node.schemaObject.setLabel(label.value);

    emit('save');
}

function cancel() {
    emit('cancel');
}

function deleteFunction() {
    props.node.neighbours.forEach(neighbour => {
        evocat.graph.schemaCategory.deleteMorphism(neighbour.edge.schemaMorphism);
        evocat.graph.deleteEdge(neighbour.edge);
    });

    evocat.graph.schemaCategory.deleteObject(props.node.schemaObject);
    evocat.graph.deleteNode(props.node);

    emit('save');
}

function startAddingId() {
    addingId.value = true;
}

function finishAddingId() {
    addingId.value = false;
}

function cancelAddingId() {
    addingId.value = false;
}
</script>

<template>
    <div>
        <h2>Edit Schema Object</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input
                    v-model="label"
                    :disabled="!isNew"
                />
            </ValueRow>
            <ValueRow label="Iri:">
                <IriDisplay
                    :iri="node.schemaObject.iri"
                    :max-chars="36"
                />
            </ValueRow>
            <ValueRow label="Pim Iri:">
                <IriDisplay
                    :iri="node.schemaObject.pimIri"
                    :max-chars="36"
                />
            </ValueRow>
            <ValueRow label="Key:">
                {{ node.schemaObject.key.toString() }}
            </ValueRow>
            <ValueRow label="Ids:">
                <ObjectIdsDisplay
                    v-if="node.schemaObject.ids"
                    :ids="node.schemaObject.ids"
                    :disabled="!isNew"
                    class="object-ids-display"
                    @delete-signature="(index) => node.deleteSignatureId(index)"
                    @delete-non-signature="() => node.deleteNonSignatureId()"
                />
                <ButtonIcon
                    v-if="!addingId && isNew && (!node.schemaObject.ids || node.schemaObject.ids.isSignatures)"
                    @click="startAddingId"
                >
                    <IconPlusSquare />
                </ButtonIcon>
            </ValueRow>
        </ValueContainer>
        <div
            v-if="addingId"
            class="editor"
        >
            <AddId
                :node="node"
                @save="finishAddingId"
                @cancel="cancelAddingId"
            />
        </div>
        <div class="button-row">
            <button
                v-if="isNew"
                :disabled="!label || !changed || addingId"
                @click="save"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
            <button
                @click="deleteFunction"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.object-ids-display {
    margin-left: -6px;
}
</style>

