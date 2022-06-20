<script lang="ts">
import { Edge, SelectionType, type Graph, type Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import type { SchemaCategoryFromServer } from '@/types/schema';
import { PUT } from '@/utils/backendAPI';
import AddObject from './AddObject.vue';
import AddMorphism from './AddMorphism.vue';
import EditObject from './EditObject.vue';
import EditMorphism from './EditMorphism.vue';
import Divider from '@/components/layout/Divider.vue';

enum State {
    Default,
    AddObject,
    AddMorphism,
    EditObject,
    EditMorphism
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddObject, unknown> |
    GenericStateValue<State.AddMorphism, unknown> |
    GenericStateValue<State.EditObject, { node: Node }> |
    GenericStateValue<State.EditMorphism, { edge: Edge }>;

export default defineComponent({
    components: {
        AddObject,
        AddMorphism,
        EditObject,
        EditMorphism,
        Divider
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    data() {
        return {
            state: { type: State.Default } as StateValue,
            State
        };
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
        this.graph.addEdgeListener('tap', this.onEdgeTapHandler);
    },
    unmounted() {
        this.graph.removeNodeListener('tap', this.onNodeTapHandler);
        this.graph.removeEdgeListener('tap', this.onEdgeTapHandler);
    },
    methods: {
        addObjectClicked() {
            this.state = { type: State.AddObject };
        },
        addMorphismClicked() {
            this.state = { type: State.AddMorphism };
        },
        setStateToDefault() {
            if (this.state.type === State.EditObject)
                this.state.node.unselect();

            if (this.state.type === State.EditMorphism)
                this.state.edge.unselect();

            this.state = { type: State.Default };
        },
        onNodeTapHandler(node: Node) {
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
                    this.state.node.unselect();
                }
            }

            node.select({ type: SelectionType.Root, level: 0 });
            this.state = { type: State.EditObject, node };
        },
        onEdgeTapHandler(edge: Edge) {
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
        async save() {
            const updateObject = this.graph.schemaCategory.getUpdateObject();
            console.log(updateObject);

            const result = await PUT<SchemaCategoryFromServer>(`/schemaCategories/${this.graph.schemaCategory.id}`, updateObject);

            console.log(result);
            /*
            if (result.status)
                this.$router.push({ name: 'jobs' });
            */
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
