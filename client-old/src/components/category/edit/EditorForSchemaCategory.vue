<script setup lang="ts">
import { Edge, SelectionType, type Node } from '@/types/categoryGraph';
import { onMounted, onUnmounted, ref, shallowRef } from 'vue';
import AddObject from './AddObject.vue';
import AddMorphism from './AddMorphism.vue';
import AddComplexStructure from './AddComplexStructure.vue';
import UpdateObjex from './UpdateObjex.vue';
import UpdateMorphism from './UpdateMorphism.vue';
import Divider from '@/components/layout/Divider.vue';
import { isKeyPressed, Key } from '@/utils/keyboardInput';
import EditGroup from './EditGroup.vue';
import { useEvocat } from '@/utils/injects';

const { evocat, graph } = $(useEvocat());

enum State {
    Default,
    AddObject,
    AddMorphism,
    AddComplexStructure,
    UpdateObjex,
    UpdateMorphism,
    EditGroup,
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue =
    GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddObject, unknown> |
    GenericStateValue<State.AddMorphism, unknown> |
    GenericStateValue<State.AddComplexStructure, unknown> |
    GenericStateValue<State.UpdateObjex, { node: Node }> |
    GenericStateValue<State.UpdateMorphism, { edge: Edge }> |
    GenericStateValue<State.EditGroup, { nodes: Node[] }>;

const state = shallowRef<StateValue>({ type: State.Default });

const editedObject = ref<InstanceType<typeof UpdateObjex>>();
const editedMorphism = ref<InstanceType<typeof UpdateMorphism>>();

const listener = graph.listen();

onMounted(() => {
    listener.onNode('tap', onNodeTapHandler);
    listener.onEdge('tap', onEdgeTapHandler);
    listener.onCanvas('tap', onCanvasTapHandler);
});

onUnmounted(() => {
    listener.close();
});

function addObjectClicked() {
    state.value = { type: State.AddObject };
}

function addMorphismClicked() {
    state.value = { type: State.AddMorphism };
}

function addComplexStructureClicked() {
    state.value = { type: State.AddComplexStructure };
}

function setStateToDefault() {
    if (state.value.type === State.UpdateObjex)
        state.value.node.unselect();

    if (state.value.type === State.UpdateMorphism)
        state.value.edge.unselect();

    if (state.value.type === State.EditGroup)
        state.value.nodes.forEach(node => node.unselect());

    state.value = { type: State.Default };
}

function onNodeTapHandler(node: Node) {
    if (state.value.type === State.EditGroup) {
        if (isKeyPressed(Key.Shift)) {
            const currentLength = state.value.nodes.length;
            state.value.nodes = state.value.nodes.filter(n => !n.equals(node));

            if (state.value.nodes.length < currentLength) {
                node.unselect();

                if (state.value.nodes.length === 1) {
                    state.value = { type: State.UpdateObjex, node: state.value.nodes[0] };
                    return;
                }
            }
            else {
                state.value.nodes.push(node);
                node.select({ type: SelectionType.Root, level: 0 });
                return;
            }
        }
        else {
            // Disband the group and then proceed in the same way as if the node was clicked.
            state.value.nodes.forEach(n => n.unselect());
            node.select({ type: SelectionType.Root, level: 0 });
            state.value = { type: State.UpdateObjex, node };
            return;
        }
    }

    if (state.value.type !== State.Default && state.value.type !== State.UpdateObjex)
        return;

    if (state.value.type === State.UpdateObjex) {
        if (editedObject.value?.changed) {
            return;
        }
        else if (state.value.node.equals(node)) {
            setStateToDefault();
            return;
        }
        else {
            if (isKeyPressed(Key.Shift)) {
                state.value = { type: State.EditGroup, nodes: [ state.value.node, node ] };
                node.select({ type: SelectionType.Root, level: 0 });
                return;
            }
            else {
                state.value.node.unselect();
            }
        }
    }

    node.select({ type: SelectionType.Root, level: 0 });
    state.value = { type: State.UpdateObjex, node };
}

function onEdgeTapHandler(edge: Edge) {
    if (state.value.type === State.UpdateObjex) {
        if (editedObject.value?.changed)
            return;

        state.value.node.unselect();
        state.value = { type: State.UpdateMorphism, edge };
        return;
    }

    if (state.value.type !== State.Default && state.value.type !== State.UpdateMorphism)
        return;

    if (state.value.type === State.UpdateMorphism) {
        if (editedMorphism.value?.changed) {
            return;
        }
        else if (state.value.edge.equals(edge)) {
            setStateToDefault();
            return;
        }
    }

    state.value = { type: State.UpdateMorphism, edge };
}

function onCanvasTapHandler() {
    if (state.value.type === State.UpdateObjex) {
        if (editedObject.value?.changed)
            return;
        setStateToDefault();
    }

    if (state.value.type === State.UpdateMorphism) {
        if (editedMorphism.value?.changed)
            return;
        setStateToDefault();
    }

    if (state.value.type === State.EditGroup)
        setStateToDefault();
}

async function save() {
    evocat.update();
}
</script>

<template>
    <div class="editor">
        <div
            v-if="state.type === State.Default"
            class="options"
        >
            <button @click="addObjectClicked">
                Add object
            </button>
            <button @click="addMorphismClicked">
                Add morphism
            </button>
            <button @click="addComplexStructureClicked">
                More ...
            </button>
            <Divider />
            <button>
                Copy / Move
            </button>
            <button>
                Group / Ungroup
            </button>
            <button>
                Split
            </button>
            <Divider />
            <button @click="save">
                Save
            </button>
        </div>
        <template v-else-if="state.type === State.AddObject">
            <AddObject
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.AddMorphism">
            <AddMorphism
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.AddComplexStructure">
            <AddComplexStructure
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.UpdateObjex">
            <UpdateObjex
                ref="editedObject"
                :key="state.node.schemaObjex.key.value"
                :node="state.node"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.UpdateMorphism">
            <UpdateMorphism
                ref="editedMorphism"
                :key="state.edge.schemaMorphism.signature.value"
                :edge="state.edge"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.EditGroup">
            <EditGroup
                :nodes="state.nodes"
                @cancel="setStateToDefault"
            />
        </template>
    </div>
</template>

<style scoped>
.options {
    display: flex;
    flex-direction: column;
}

.options button {
    margin-top: 6px;
    margin-bottom: 6px;
}

.options button:first-of-type {
    margin-top: 0px;
}

.options button:last-of-type {
    margin-bottom: 0px;
}
</style>
