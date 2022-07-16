<script lang="ts">
import type { ComplexProperty, RootProperty, ChildProperty, ParentProperty } from '@/types/accessPath/graph';
import type { Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import ParentPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { DatabaseView } from '@/types/database';
import AddProperty from './AddProperty.vue';
import EditProperty from './EditProperty.vue';
import StaticNameInput from '../input/StaticNameInput.vue';

enum State {
    Default,
    AddProperty,
    EditProperty
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddProperty, { parent: ParentProperty }> |
    GenericStateValue<State.EditProperty, { property: ChildProperty }>;
/*
type State = { default: string }
    | { editProperty: { property: ComplexProperty } };
*/

export default defineComponent({
    components: {
        AddProperty,
        EditProperty,
        ParentPropertyDisplay,
        StaticNameInput
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        database: {
            type: Object as () => DatabaseView,
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
            name: '',
            state: { type: State.Default } as StateValue,
            State,
        };
    },
    methods: {
        editPropertyClicked(property: ChildProperty) {
            this.state = {
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
            this.$emit('finish', this.name);
        }
    }
});
</script>

<template>
    <div class="divide">
        <div>
            <div class="editor">
                <template v-if="state.type === State.Default">
                    <table>
                        <tr>
                            <td class="label">
                                Database:
                            </td>
                            <td class="value">
                                {{ database.label }}
                            </td>
                        </tr>
                        <tr>
                            <td class="label">
                                Root object:
                            </td>
                            <td class="value">
                                {{ rootProperty.node.label }}
                            </td>
                        </tr>
                        <tr>
                            <td class="label">
                                Name:
                            </td>
                            <td class="value">
                                <input v-model="name" />
                            </td>
                        </tr>
                        <tr>
                            <td class="label">
                                Kind name:
                            </td>
                            <td class="value">
                                <StaticNameInput v-model="rootProperty.name" />
                            </td>
                        </tr>
                    </table>
                    <div class="button-row">
                        <button
                            :disabled="!name"
                            @click="finishMapping"
                        >
                            Finish mapping
                        </button>
                    </div>
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
        </div>
        <ParentPropertyDisplay
            :property="rootProperty"
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
