<script setup lang="ts">
import type { GraphComplexProperty, GraphRootProperty, GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph';
import type { Graph } from '@/types/categoryGraph';
import { ref } from 'vue';
import ParentPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { DatabaseWithConfiguration } from '@/types/database';
import AddProperty from './AddProperty.vue';
import EditProperty from './EditProperty.vue';
import StaticNameInput from '../input/StaticNameInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import PrimaryKeyInput from '../input/PrimaryKeyInput.vue';
import { SignatureIdFactory } from '@/types/identifiers';

enum State {
    Default,
    AddProperty,
    EditProperty
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.AddProperty, { parent: GraphParentProperty }> |
    GenericStateValue<State.EditProperty, { property: GraphChildProperty }>;

type AccessPathEditor = {
    graph: Graph;
    database: DatabaseWithConfiguration;
    rootProperty: GraphRootProperty;
};

const props = defineProps<AccessPathEditor>();

const emit = defineEmits([ 'finish' ]);

const ids = props.rootProperty.node.schemaObject.ids!;

const label = ref('');
const state = ref<StateValue>({ type: State.Default });
const primaryKey = ref((ids.isSignatures && ids.signatureIds.length > 0) ? ids.signatureIds[0] : SignatureIdFactory.createEmpty());

function editPropertyClicked(property: GraphChildProperty) {
    state.value = {
        type: State.EditProperty,
        property
    };
}

function addPropertyClicked(parentProperty: GraphComplexProperty) {
    state.value = {
        type: State.AddProperty,
        parent: parentProperty
    };
}

function setStateToDefault() {
    state.value = { type: State.Default };
}

function finishMapping() {
    emit('finish', label.value, primaryKey.value);
}
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
                        <ValueRow
                            v-if="rootProperty.node.schemaObject.ids"
                            label="Primary key:"
                        >
                            <PrimaryKeyInput
                                v-model="primaryKey"
                                :ids="rootProperty.node.schemaObject.ids"
                            />
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
