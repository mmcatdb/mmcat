<script lang="ts">
import { Edge, SelectionType, type Graph, type Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import { SchemaCategory } from '@/types/schema';
import AddObject from './AddObject.vue';
import AddMorphism from './AddMorphism.vue';
import EditObject from './EditObject.vue';
import EditMorphism from './EditMorphism.vue';
import Integration from './Integration.vue';
import Divider from '@/components/layout/Divider.vue';
import API from '@/utils/api';
import { isKeyPressed, Key } from '@/utils/keyboardInput';
import EditGroup from './EditGroup.vue';

enum State {
    Default,
    AddObject,
    AddMorphism,
    EditObject,
    EditMorphism,
    EditGroup,
    Integration
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue =
    GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddObject, unknown> |
    GenericStateValue<State.AddMorphism, unknown> |
    GenericStateValue<State.EditObject, { node: Node }> |
    GenericStateValue<State.EditMorphism, { edge: Edge }> |
    GenericStateValue<State.EditGroup, { nodes: Node[] }> |
    GenericStateValue<State.Integration, unknown>;

export default defineComponent({
    components: {
        AddObject,
        AddMorphism,
        EditObject,
        EditMorphism,
        Divider,
        Integration,
        EditGroup
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'save' ],
    data() {
        return {
            state: { type: State.Default } as StateValue,
            State
        };
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
        this.graph.addEdgeListener('tap', this.onEdgeTapHandler);
        this.graph.addCanvasListener('tap', this.onCanvasTapHandler);
    },
    unmounted() {
        this.graph.removeNodeListener('tap', this.onNodeTapHandler);
        this.graph.removeEdgeListener('tap', this.onEdgeTapHandler);
        this.graph.removeCanvasListener('tap', this.onCanvasTapHandler);
    },
    methods: {
        addObjectClicked() {
            this.state = { type: State.AddObject };
        },
        addMorphismClicked() {
            this.state = { type: State.AddMorphism };
        },
        integrationClicked() {
            this.state = { type: State.Integration };
        },
        setStateToDefault() {
            if (this.state.type === State.EditObject)
                this.state.node.unselect();

            if (this.state.type === State.EditMorphism)
                this.state.edge.unselect();

            if (this.state.type === State.EditGroup)
                this.state.nodes.forEach(node => node.unselect());

            this.state = { type: State.Default };
        },
        onNodeTapHandler(node: Node) {
            if (this.state.type === State.EditGroup) {
                if (isKeyPressed(Key.Shift)) {
                    const currentLength = this.state.nodes.length;
                    this.state.nodes = this.state.nodes.filter(n => !n.equals(node));

                    if (this.state.nodes.length < currentLength) {
                        node.unselect();

                        if (this.state.nodes.length === 1) {
                            this.state = { type: State.EditObject, node: this.state.nodes[0] };
                            return;
                        }
                    }
                    else {
                        this.state.nodes.push(node);
                        node.select({ type: SelectionType.Root, level: 0 });
                        return;
                    }
                }
                else {
                    // Disband the group and then proceed in the same way as if the node was clicked.
                    this.state.nodes.forEach(n => n.unselect());
                    node.select({ type: SelectionType.Root, level: 0 });
                    this.state = { type: State.EditObject, node };
                    return;
                }
            }

            if (this.state.type !== State.Default && this.state.type !== State.EditObject)
                return;

            if (this.state.type === State.EditObject) {
                if ((this.$refs.editedObject as InstanceType<typeof EditObject>).changed) {
                    return;
                }
                else if (this.state.node.equals(node)) {
                    this.setStateToDefault();
                    return;
                }
                else {
                    if (isKeyPressed(Key.Shift)) {
                        this.state = { type: State.EditGroup, nodes: [ this.state.node, node ]};
                        node.select({ type: SelectionType.Root, level: 0 });
                        return;
                    }
                    else {
                        this.state.node.unselect();
                    }
                }
            }

            node.select({ type: SelectionType.Root, level: 0 });
            this.state = { type: State.EditObject, node };
        },
        onEdgeTapHandler(edge: Edge) {
            if (this.state.type === State.EditObject) {
                if ((this.$refs.editedObject as InstanceType<typeof EditObject>).changed)
                    return;

                this.state.node.unselect();
                this.state = { type: State.EditMorphism, edge };
                return;
            }

            if (this.state.type !== State.Default && this.state.type !== State.EditMorphism)
                return;

            if (this.state.type === State.EditMorphism) {
                if ((this.$refs.editedMorphism as InstanceType<typeof EditMorphism>).changed) {
                    return;
                }
                else if (this.state.edge.equals(edge)) {
                    this.setStateToDefault();
                    return;
                }
                else {
                    this.state.edge.unselect();
                }
            }

            this.state = { type: State.EditMorphism, edge };
        },
        onCanvasTapHandler() {
            if (this.state.type === State.EditObject) {
                if ((this.$refs.editedObject as InstanceType<typeof EditObject>).changed)
                    return;
                this.setStateToDefault();
            }

            if (this.state.type === State.EditMorphism) {
                if ((this.$refs.editedMorphism as InstanceType<typeof EditMorphism>).changed)
                    return;
                this.setStateToDefault();
            }

            if (this.state.type === State.EditGroup)
                this.setStateToDefault();
        },
        async save() {
            const updateObject = this.graph.schemaCategory.getUpdateObject();

            const result = await API.schemas.updateCategoryWrapper({ id: this.graph.schemaCategory.id }, updateObject);
            if (result.status) {
                const schemaCategory = SchemaCategory.fromServer(result.data);
                this.$emit('save', schemaCategory);
            }
        }
    }
});
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
        <template v-else-if="state.type === State.EditObject">
            <EditObject
                ref="editedObject"
                :key="state.node.schemaObject.id"
                :graph="graph"
                :node="state.node"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
        <template v-else-if="state.type === State.EditMorphism">
            <EditMorphism
                ref="editedMorphism"
                :key="state.edge.schemaMorphism.id"
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

/*
.options button + button {
    margin-top: 12px;
}
*/

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
