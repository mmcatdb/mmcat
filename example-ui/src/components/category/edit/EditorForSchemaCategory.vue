<script lang="ts">
import { SelectionType, type Graph, type Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import type { SchemaCategoryFromServer } from '@/types/schema';
import { PUT } from '@/utils/backendAPI';
import AddObject from './AddObject.vue';
import AddMorphism from './AddMorphism.vue';
import EditObject from './EditObject.vue';

enum State {
    Default,
    AddObject,
    AddMorphism,
    EditObject
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddObject, unknown> |
    GenericStateValue<State.AddMorphism, unknown> |
    GenericStateValue<State.EditObject, { node: Node }>;

export default defineComponent({
    components: {
        AddObject,
        AddMorphism,
        EditObject
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
    },
    unmounted() {
        this.graph.removeListener('tap', this.onNodeTapHandler);
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

            this.state = { type: State.Default };
        },
        onNodeTapHandler(node: Node) {
            if (this.state.type !== State.Default)
                return;

            node.select({ type: SelectionType.Root, level: 0 });
            this.state = { type: State.EditObject, node };
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
                :graph="graph"
                :node="state.node"
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
    </div>
</template>

<style scoped>
.editor {
    border: 2px solid var(--color-border);
    padding: 12px;
}

.options {
    display: flex;
    flex-direction: column;
}

.options button + button {
    margin-top: 12px;
}
</style>
