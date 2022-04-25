<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import AddObject from './AddObject.vue';
import AddMorphism from './AddMorphism.vue';
import type { SchemaCategoryFromServer } from '@/types/schema';
import { PUT } from '@/utils/backendAPI';

enum State {
    Default,
    AddObject,
    AddMorphism
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddObject, unknown> |
    GenericStateValue<State.AddMorphism, unknown>;

export default defineComponent({
    components: {
        AddObject,
        AddMorphism
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
    methods: {
        addObjectClicked() {
            this.state = { type: State.AddObject };
        },
        addMorphismClicked() {
            this.state = { type: State.AddMorphism };
        },
        setStateToDefault(): void {
            this.state = { type: State.Default };
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
        <template v-if="state.type === State.Default">
            <button @click="addObjectClicked">
                Add object
            </button>
            <button @click="addMorphismClicked">
                Add morphism
            </button>
            <button @click="save">
                Save
            </button>
        </template>
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
    </div>
</template>

<style scoped>
.editor {
    padding: 12px;
    display: flex;
    flex-direction: column;
}
</style>
