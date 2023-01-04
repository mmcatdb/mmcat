<script lang="ts">
import type { GraphComplexProperty, GraphRootProperty, GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph';
import type { Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import ParentPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { DatabaseWithConfiguration } from '@/types/database';
import AddProperty from './AddProperty.vue';
import EditProperty from './EditProperty.vue';
import StaticNameInput from '../input/StaticNameInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

enum State {
    Default,
    AddProperty,
    EditProperty
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddProperty, { parent: GraphParentProperty }> |
    GenericStateValue<State.EditProperty, { property: GraphChildProperty }>;

export default defineComponent({
    components: {
        AddProperty,
        EditProperty,
        ParentPropertyDisplay,
        StaticNameInput,
        ValueContainer,
        ValueRow
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        database: {
            type: Object as () => DatabaseWithConfiguration,
            required: true
        },
        rootProperty: {
            type: Object as () => GraphRootProperty,
            required: true
        }
    },
    emits: [ 'finish' ],
    data() {
        return {
            label: '',
            state: { type: State.Default } as StateValue,
            State,
        };
    },
    methods: {
        editPropertyClicked(property: GraphChildProperty) {
            this.state = {
                type: State.EditProperty,
                property
            };
        },
        addPropertyClicked(parentProperty: GraphComplexProperty) {
            this.state = {
                type: State.AddProperty,
                parent: parentProperty
            };
        },
        setStateToDefault() {
            this.state = { type: State.Default };
        },
        finishMapping() {
            this.$emit('finish', this.label);
        }
    }
});
</script>

<template>
    <div class="divide">
        <div>
            <div class="editor">
                <template v-if="state.type === State.Default">
                    <ValueContainer>
                        <ValueRow label="Database:">
                            {{ database.label }}
                        </ValueRow>
                        <ValueRow label="Root object:">
                            {{ rootProperty.node.label }}
                        </ValueRow>
                        <ValueRow label="Label:">
                            <input v-model="label" />
                        </ValueRow>
                        <ValueRow label="Kind name:">
                            <StaticNameInput v-model="rootProperty.name" />
                        </ValueRow>
                    </ValueContainer>
                    <div class="button-row">
                        <button
                            :disabled="!label"
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
