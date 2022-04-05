<script lang="ts">
import type { ComplexProperty, RootProperty, ChildProperty, ParentProperty } from '@/types/accessPath/graph';
import type { Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import EditProperty from './EditProperty.vue';
import ComplexPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { Database } from '@/types/database';
import AddProperty from './AddProperty.vue';

enum State {
    Default,
    EditProperty,
    AddProperty,
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown>
    | GenericStateValue<State.EditProperty, { property: ChildProperty }>
    | GenericStateValue<State.AddProperty, { parent: ParentProperty }>;
/*
type State = { default: string }
    | { editProperty: { property: ComplexProperty } };
*/

export default defineComponent({
    components: {
        EditProperty,
        ComplexPropertyDisplay,
        AddProperty
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        database: {
            type: Object as () => Database,
            required: true
        },
        rootProperty: {
            type: Object as () => RootProperty,
            required: true
        }
    },
    data() {
        return {
            rootObjectName: 'pathName',
            //state: { default: '' } as State,
            state: { type: State.Default } as StateValue,
            State,
        };
    },
    methods: {
        editPropertyClicked(property: ChildProperty) {
            this.state = { type: State.EditProperty, property };
        },
        addPropertyClicked(parentProperty: ComplexProperty) {
            this.state = {
                type: State.AddProperty,
                parent: parentProperty
            };
        },
        setStateToDefault(): void {
            this.state = { type: State.Default };
        }
    }
});
</script>

<template>
    <div class="divide">
        <div class="editor">
            <template v-if="state.type === State.Default">
                DEFAULT
            </template>
            <template v-else-if="state.type === State.AddProperty">
                <AddProperty
                    :graph="graph"
                    :database="database"
                    :parent-property="state.parent"
                    @save="setStateToDefault"
                    @cancel="setStateToDefault"
                />
            </template>
            <template v-else-if="state.type === State.EditProperty">
                <EditProperty
                    :graph="graph"
                    :database="database"
                    :property="state.property"
                    @save="setStateToDefault"
                    @cancel="setStateToDefault"
                />
            </template>
        </div>
        <div class="display">
            <ComplexPropertyDisplay
                :property="rootProperty"
                :is-last="true"
                @complex:click="editPropertyClicked"
                @simple:click="editPropertyClicked"
                @add:click="addPropertyClicked"
            />
        </div>
    </div>
</template>

<style scoped>
.accessPathInput {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}

.editor {
    padding: 12px;
    display: flex;
    flex-direction: column;
}

.divide {
    display: flex;
}

.display {
    padding: 16px;
    margin: 16px;
}

.createProperty {
    padding: 16px;
    margin: 16px;
    border: 1px solid white;
}
</style>
