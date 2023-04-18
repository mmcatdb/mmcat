<script setup lang="ts">
import { Edge, SelectionType, type Graph, type Node } from '@/types/categoryGraph';
import { onMounted, onUnmounted, ref } from 'vue';
import { SchemaCategory } from '@/types/schema';
import AddObject from './AddObject.vue';
import AddMorphism from './AddMorphism.vue';
import AddComplexStructure from './AddComplexStructure.vue';
import EditObject from './EditObject.vue';
import EditMorphism from './EditMorphism.vue';
import Integration from '../../integration/Integration.vue';
import Divider from '@/components/layout/Divider.vue';
import API from '@/utils/api';
import { isKeyPressed, Key } from '@/utils/keyboardInput';
import EditGroup from './EditGroup.vue';

enum State {
    Default,
    AddObject,
    AddMorphism,
    AddComplexStructure,
    EditObject,
    EditMorphism,
    EditGroup,
    Integration,
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue =
    GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddObject, unknown> |
    GenericStateValue<State.AddMorphism, unknown> |
    GenericStateValue<State.AddComplexStructure, unknown> |
    GenericStateValue<State.EditObject, { node: Node }> |
    GenericStateValue<State.EditMorphism, { edge: Edge }> |
    GenericStateValue<State.EditGroup, { nodes: Node[] }> |
    GenericStateValue<State.Integration, unknown>;

type EditorForSchemaCategoryProps = {
    graph: Graph;
};

const props = defineProps<EditorForSchemaCategoryProps>();

const emit = defineEmits([ 'save' ]);

const state = ref<StateValue>({ type: State.Default });

const editedObject = ref<InstanceType<typeof EditObject>>();
const editedMorphism = ref<InstanceType<typeof EditMorphism>>();

onMounted(() => {
    props.graph.addNodeListener('tap', onNodeTapHandler);
    props.graph.addEdgeListener('tap', onEdgeTapHandler);
    props.graph.addCanvasListener('tap', onCanvasTapHandler);
});

onUnmounted(() => {
    props.graph.removeNodeListener('tap', onNodeTapHandler);
    props.graph.removeEdgeListener('tap', onEdgeTapHandler);
    props.graph.removeCanvasListener('tap', onCanvasTapHandler);
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

function integrationClicked() {
    state.value = { type: State.Integration };
}

function setStateToDefault() {
    if (state.value.type === State.EditObject)
        state.value.node.unselect();

    if (state.value.type === State.EditMorphism)
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
                    state.value = { type: State.EditObject, node: state.value.nodes[0] };
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
            state.value = { type: State.EditObject, node };
            return;
        }
    }

    if (state.value.type !== State.Default && state.value.type !== State.EditObject)
        return;

    if (state.value.type === State.EditObject) {
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
    state.value = { type: State.EditObject, node };
}

function onEdgeTapHandler(edge: Edge) {
    if (state.value.type === State.EditObject) {
        if (editedObject.value?.changed)
            return;

        state.value.node.unselect();
        state.value = { type: State.EditMorphism, edge };
        return;
    }

    if (state.value.type !== State.Default && state.value.type !== State.EditMorphism)
        return;

    if (state.value.type === State.EditMorphism) {
        if (editedMorphism.value?.changed) {
            return;
        }
        else if (state.value.edge.equals(edge)) {
            setStateToDefault();
            return;
        }
        else {
            state.value.edge.unselect();
        }
    }

    state.value = { type: State.EditMorphism, edge };
}

function onCanvasTapHandler() {
    if (state.value.type === State.EditObject) {
        if (editedObject.value?.changed)
            return;
        setStateToDefault();
    }

    if (state.value.type === State.EditMorphism) {
        if (editedMorphism.value?.changed)
            return;
        setStateToDefault();
    }

    if (state.value.type === State.EditGroup)
        setStateToDefault();
}

async function save() {
    const updateObject = props.graph.schemaCategory.getUpdateObject();
    console.log(props.graph.schemaCategory);
    if (!updateObject) {
        console.log('Update object invalid');
        return;
    }

    const result = await API.schemas.updateCategoryWrapper({ id: props.graph.schemaCategory.id }, updateObject);
    if (result.status) {
        const schemaCategory = SchemaCategory.fromServer(result.data);
        emit('save', schemaCategory);
    }
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
            <button @click="integrationClicked">
                Integration
            </button>
            <Divider />
            <button @click="save">
                Save
            </button>
        </div>
        <template v-else-if="state.type === State.AddObject">
            <AddObject
                :graph="graph"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.AddMorphism">
            <AddMorphism
                :graph="graph"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.AddComplexStructure">
            <AddComplexStructure
                :graph="graph"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.EditObject">
            <EditObject
                ref="editedObject"
                :key="state.node.schemaObject.key.toString()"
                :graph="graph"
                :node="state.node"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.EditMorphism">
            <EditMorphism
                ref="editedMorphism"
                :key="state.edge.schemaMorphism.signature.toString()"
                :graph="graph"
                :edge="state.edge"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.Integration">
            <Integration
                :graph="graph"
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
