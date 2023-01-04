<script lang="ts">
import { GraphSimpleProperty, GraphComplexProperty, type GraphChildProperty } from '@/types/accessPath/graph';
import { PropertyType, type Graph, createDefaultFilter, type Node } from '@/types/categoryGraph';
import type { Name } from '@/types/identifiers';
import { defineComponent } from 'vue';
import type { DatabaseWithConfiguration } from '@/types/database';
import type { SchemaObject } from '@/types/schema';

import SignatureInput from '../input/SignatureInput.vue';
import TypeInput from '../input/TypeInput.vue';
import NameInput from '../input/NameInput.vue';
import ObjectIdsDisplay from '@/components/category/ObjectIdsDisplay.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

enum State {
    SelectSignature,
    SelectType,
    SelectName
}

export default defineComponent({
    components: {
        SignatureInput,
        TypeInput,
        NameInput,
        ObjectIdsDisplay,
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
        property: {
            type: Object as () => GraphChildProperty,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            type: this.propertyToType(this.property),
            PropertyType,
            signature: this.property.signature.copy(),
            name: this.property.name.copy() as Name,
            state: State.SelectSignature,
            State,
            filter: createDefaultFilter(this.database.configuration),
            typeIsDetermined: false
        };
    },
    computed: {
        typeChanged(): boolean {
            return this.type !== this.propertyToType(this.property);
        },
        nameChanged(): boolean {
            return !this.property.name.equals(this.name);
        },
        signatureChanged(): boolean {
            return !this.property.signature.equals(this.signature);
        },
        schemaObject(): SchemaObject {
            return this.signature.sequence.lastNode.schemaObject;
        }
    },
    methods: {
        propertyToType(property: GraphChildProperty): PropertyType {
            return property instanceof GraphSimpleProperty ? PropertyType.Simple : PropertyType.Complex;
        },
        save() {
            const subpaths = !this.signatureChanged && !this.typeChanged && this.property instanceof GraphComplexProperty ? this.property.subpaths : [];
            const newProperty = this.type === PropertyType.Simple
                ? new GraphSimpleProperty(this.name, this.signature, this.property.parent)
                : new GraphComplexProperty(this.name, this.signature, this.property.parent, subpaths);

            this.property.parent.updateOrAddSubpath(newProperty, this.property);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirmSignature() {
            const node = this.signature.sequence.lastNode;
            const type = this.determinePropertyType(node);

            if (type !== null) {
                this.type = type;
                this.typeIsDetermined = true;
                this.state = State.SelectName;
            }
            else {
                this.state = State.SelectType;
                this.typeIsDetermined = false;
            }
        },
        determinePropertyType(node: Node): PropertyType | null {
            if (!this.database.configuration.isComplexPropertyAllowed)
                return PropertyType.Simple;

            // Auxiliary property.
            if (this.signature.isNull)
                return PropertyType.Complex;

            return node.determinedPropertyType;
        },
        resetSignature() {
            this.signature = this.property.signature.copy();
        },
        confirmType() {
            this.state = State.SelectName;
        },
        resetType() {
            this.type = this.propertyToType(this.property);
        },
        confirmName() {
            this.save();
        },
        resetName() {
            this.name = this.property.name.copy();
        },
        deleteProperty() {
            this.property.parent.removeSubpath(this.property);
            this.$emit('save');
        },
        backButton() {
            this.state--;
            if (this.state === State.SelectType && this.typeIsDetermined)
                this.state--;
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Edit property</h2>
        <ValueContainer>
            <ValueRow label="Object:">
                {{ schemaObject.label }}
            </ValueRow>
            <ValueRow label="Ids:">
                <ObjectIdsDisplay
                    v-if="schemaObject.ids"
                    :ids="schemaObject.ids"
                    disabled
                    class="object-ids-display"
                />
            </ValueRow>
            <ValueRow
                v-if="state >= State.SelectSignature"
                label="Signature:"
            >
                {{ signature }}
            </ValueRow>
            <ValueRow
                v-if="state >= State.SelectName"
                label="Type:"
            >
                {{ type }}
            </ValueRow>
            <ValueRow
                v-if="state === State.SelectType"
                label="Type:"
            >
                <TypeInput v-model="type" />
            </ValueRow>
            <ValueRow
                v-if="state === State.SelectName"
                label="Name:"
            >
                <NameInput
                    v-model="name"
                    :graph="graph"
                    :database="database"
                    :root-node="property.parentNode"
                />
            </ValueRow>
        </ValueContainer>
        <div
            v-if="state === State.SelectSignature"
            class="button-row"
        >
            <SignatureInput
                v-model="signature"
                :graph="graph"
                :filter="filter"
                :default-is-null="true"
            >
                <template #nullButton>
                    Auxiliary property
                </template>
            </SignatureInput>
        </div>
        <div class="button-row">
            <template v-if="state === State.SelectSignature">
                <button
                    :disabled="!database.configuration.isGrouppingAllowed && signature.isNull"
                    @click="confirmSignature"
                >
                    {{ signatureChanged ? 'Confirm change' : 'Keep current' }}
                </button>
                <button
                    v-if="signatureChanged"
                    @click="resetSignature"
                >
                    Reset
                </button>
            </template>
            <template v-if="state === State.SelectType">
                <button
                    @click="confirmType"
                >
                    {{ typeChanged ? 'Confirm change' : 'Keep current' }}
                </button>
                <button
                    v-if="typeChanged"
                    @click="resetType"
                >
                    Reset
                </button>
            </template>
            <template v-if="state === State.SelectName">
                <button
                    @click="confirmName"
                >
                    {{ nameChanged ? 'Confirm change' : 'Keep current' }}
                </button>
                <button
                    v-if="nameChanged"
                    @click="resetName"
                >
                    Reset
                </button>
            </template>
        </div>
        <div class="button-row">
            <button
                v-if="state > State.SelectSignature"
                @click="backButton"
            >
                Back
            </button>
            <button @click="cancel">
                Cancel
            </button>
            <button
                @click="deleteProperty"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.object-ids-display {
    margin-left: -6px;
}
</style>

