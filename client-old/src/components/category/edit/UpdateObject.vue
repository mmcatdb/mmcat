<script setup lang="ts">
import type { Node } from '@/types/categoryGraph';
import { computed, shallowRef, watch } from 'vue';
import ObjectIdsDisplay from '@/components/category/ObjectIdsDisplay.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import ButtonIcon from '@/components/common/ButtonIcon.vue';
import IdInput from './IdInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { useEvocat } from '@/utils/injects';
import { ObjectIds, idsAreEqual } from '@/types/identifiers/ObjectIds';

const { evocat } = $(useEvocat());

type UpdateObjectProps = {
    node: Node;
};

const props = defineProps<UpdateObjectProps>();

const emit = defineEmits([ 'save', 'cancel', 'update' ]);

const label = shallowRef(props.node.schemaObject.label);
const changed = computed(() => label.value !== props.node.schemaObject.label || !idsAreEqual(objectIds.value, props.node.schemaObject.ids) || addingId.value);
const isNew = computed(() => props.node.schemaObject.isNew);

defineExpose({ changed });

function save() {
    const update = {
        ...props.node.schemaObject.toDefinition(),
        label: label.value.trim(),
        ids: objectIds.value,
    };
    evocat.updateObject(update, props.node.schemaObject);
    emit('save');
}

function cancel() {
    emit('cancel');
    console.log(props.node.cytoscapeIdAndPosition);
    console.log(props.node.schemaObject);
}

function deleteFunction() {
    props.node.neighbors.forEach(neighbor => {
        evocat.deleteMorphism(neighbor.edge.schemaMorphism);
    });

    evocat.deleteObject(props.node.schemaObject);
    emit('save');
}

const addingId = shallowRef(false);
const objectIds = shallowRef(props.node.schemaObject.ids);

watch(objectIds, () => addingId.value = false);

function startAddingId() {
    addingId.value = true;
}

function cancelAddingId() {
    addingId.value = false;
}

function deleteSignatureId(index: number) {
    if (!objectIds.value?.isSignatures)
        return;

    const newIds = objectIds.value.signatureIds.filter((_, i) => i !== index);
    objectIds.value = newIds.length > 0 ? ObjectIds.createSignatures(newIds) : undefined;
}

function deleteNonSignatureId() {
    objectIds.value = undefined;
}
</script>

<template>
    <div>
        <h2>Edit Schema Object</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input
                    v-model="label"
                />
                <!--  :disabled="!isNew"  -->
            </ValueRow>
            <ValueRow label="Key:">
                {{ node.schemaObject.key.toString() }}
            </ValueRow>
            <ValueRow label="Ids:">
                <ObjectIdsDisplay
                    v-if="objectIds"
                    :ids="objectIds"
                    :disabled="!isNew || addingId"
                    class="object-ids-display"
                    @delete-signature="deleteSignatureId"
                    @delete-non-signature="deleteNonSignatureId"
                />
                <ButtonIcon
                    v-if="!addingId && isNew && (!objectIds || objectIds.isSignatures)"
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
            <IdInput
                v-model="objectIds"
                :node="node"
                @cancel="cancelAddingId"
            />
        </div>
        <div class="button-row">
            <button
                :disabled="!label || !changed || addingId"
                @click="save"
            >
                <!-- v-if="isNew" -->
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

