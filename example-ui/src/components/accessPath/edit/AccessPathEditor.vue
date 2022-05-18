<script lang="ts">
import { type ComplexProperty, RootProperty, type ChildProperty, type ParentProperty } from '@/types/accessPath/graph';
import type { Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import ComplexPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { Database } from '@/types/database';
import AddProperty from './AddProperty.vue';
import EditProperty from './EditProperty.vue';
import EditRootProperty from './EditRootProperty.vue';

enum State {
    Default,
    AddProperty,
    EditProperty,
    EditRootProperty
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddProperty, { parent: ParentProperty }> |
    GenericStateValue<State.EditProperty, { property: ChildProperty }> |
    GenericStateValue<State.EditRootProperty, { property: RootProperty }>;
/*
type State = { default: string }
    | { editProperty: { property: ComplexProperty } };
*/

export default defineComponent({
    components: {
        AddProperty,
        EditProperty,
        EditRootProperty,
        ComplexPropertyDisplay
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
    emits: [ 'finish' ],
    data() {
        return {
            rootObjectName: 'pathName',
            //state: { default: '' } as State,
            state: { type: State.Default } as StateValue,
            State,
        };
    },
    methods: {
        editPropertyClicked(property: RootProperty | ChildProperty) {
            this.state = property instanceof RootProperty ?
                {
                    type: State.EditRootProperty,
                    property
                } :
                {
                    type: State.EditProperty,
                    property
                };
        },
        addPropertyClicked(parentProperty: ComplexProperty) {
            this.state = {
                type: State.AddProperty,
                parent: parentProperty
            };
        },
        setStateToDefault() {
            this.state = { type: State.Default };
        },
        finishMapping() {
            this.$emit('finish');
        }
    }
});
</script>

<template>
    <div class="divide">
        <div>
            <div class="editor">
                <div
                    v-if="state.type === State.Default"
                    class="options"
                >
                    <button @click="finishMapping">
                        Finish mapping
                    </button>
                </div>
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
                <template v-else-if="state.type === State.EditRootProperty">
                    <EditRootProperty
                        :graph="graph"
                        :database="database"
                        :property="state.property"
                        @save="setStateToDefault"
                        @cancel="setStateToDefault"
                    />
                </template>
            </div>
        </div>
        <ComplexPropertyDisplay
            :property="rootProperty"
            :is-last="true"
            @complex:click="editPropertyClicked"
            @simple:click="editPropertyClicked"
            @add:click="addPropertyClicked"
        />
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

.options {
    display: flex;
    flex-direction: column;
}

.createProperty {
    padding: 16px;
    margin: 16px;
    border: 1px solid white;
}
</style>
