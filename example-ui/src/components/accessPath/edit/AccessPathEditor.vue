<script lang="ts">
import { ComplexProperty, RootProperty, SimpleProperty, type ChildProperty, type ParentProperty } from '@/types/accessPath';
import { Signature, StaticName } from '@/types/identifiers';
import type { Graph, Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import EditProperty from './EditProperty.vue';
import ComplexPropertyDisplay from '../display/ComplexPropertyDisplay.vue';
import type { Database } from '@/types/database';

enum State {
    Default,
    EditProperty,
    AddProperty,
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown>
    | GenericStateValue<State.EditProperty, { property: ChildProperty, parentNode: Node }>
    | GenericStateValue<State.AddProperty, { property: ChildProperty, parent: ParentProperty }>;
/*
type State = { default: string }
    | { editProperty: { property: ComplexProperty } };
*/

export default defineComponent({
    components: {
        EditProperty,
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
        rootNode: {
            type: Object as () => Node,
            required: true
        },
        accessPath: {
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
        startAddingProperty() {
            this.state = {
                type: State.AddProperty,
                property: new ComplexProperty(StaticName.fromString(''), Signature.empty),
                parent: this.accessPath
            };
        },
        editProperty(property: ChildProperty): void {
            if (this.state.type !== State.EditProperty)
                return;

            this.state.property.parent?.updateOrAddSubpath(property, this.state.property);
            this.setStateToDefault();
        },
        addProperty(newProperty: ChildProperty): void {
            if (this.state.type !== State.AddProperty)
                return;

            this.state.parent.updateOrAddSubpath(newProperty);
            this.setStateToDefault();
        },
        editPropertyClicked(property: ChildProperty) {
            this.state = { type: State.EditProperty, property, parentNode: property.parent?.node ?? this.rootNode };
        },
        addPropertyClicked(parentProperty: ComplexProperty) {
            this.state = {
                type: State.AddProperty,
                property: new SimpleProperty(StaticName.fromString(''), Signature.empty),
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

                <div class="createProperty">
                    <button @click="startAddingProperty">
                        Create new Property
                    </button>
                </div>
            </template>
            <template v-else-if="state.type === State.EditProperty">
                <EditProperty
                    :graph="graph"
                    :database="database"
                    :parent-node="state.parentNode"
                    :property="state.property"
                    @save="editProperty"
                    @cancel="setStateToDefault"
                />
            </template>
            <template v-else-if="state.type === State.AddProperty">
                <EditProperty
                    :graph="graph"
                    :database="database"
                    :parent-node="state.parent.node"
                    :property="state.property"
                    @save="addProperty"
                    @cancel="setStateToDefault"
                />
            </template>
        </div>
        <div class="display">
            <ComplexPropertyDisplay
                :property="accessPath"
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
