<script lang="ts">
import { GraphSimpleProperty, GraphComplexProperty, SequenceSignature, type GraphParentProperty } from '@/types/accessPath/graph';
import { PropertyType, type Graph, createDefaultFilter, Node } from '@/types/categoryGraph';
import { StaticName, type Name } from '@/types/identifiers';
import { defineComponent } from 'vue';
import SignatureInput from '../input/SignatureInput.vue';
import TypeInput from '../input/TypeInput.vue';
import NameInput from '../input/NameInput.vue';
import type { DatabaseWithConfiguration } from '@/types/database';
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
        parentProperty: {
            type: Object as () => GraphParentProperty,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            type: PropertyType.Simple,
            PropertyType,
            signature: SequenceSignature.null(this.parentProperty.node),
            name: StaticName.fromString('') as Name,
            state: State.SelectSignature,
            State,
            filter: createDefaultFilter(this.database.configuration),
            typeIsDetermined: false
        };
    },
    methods: {
        save() {
            const newProperty = this.type === PropertyType.Simple
                ? new GraphSimpleProperty(this.name, this.signature, this.parentProperty)
                : new GraphComplexProperty(this.name, this.signature, this.parentProperty);

            this.parentProperty.updateOrAddSubpath(newProperty);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirmSignature() {
            const node = this.signature.sequence.lastNode;
            this.name = StaticName.fromString(node.schemaObject.label.toLowerCase());
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
        confirmType() {
            this.state = State.SelectName;
        },
        confirmName() {
            this.save();
        },
        nextButton() {
            switch (this.state) {
            case State.SelectSignature:
                this.confirmSignature();
                break;
            case State.SelectType:
                this.confirmType();
                break;
            case State.SelectName:
                this.confirmName();
                break;
            }
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
        <h2>Add property</h2>
        <ValueContainer>
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
                    :root-node="parentProperty.node"
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
            <button
                :disabled="state === State.SelectSignature && !database.configuration.isGrouppingAllowed && signature.isNull"
                @click="nextButton"
            >
                {{ state < State.SelectName ? 'Next' : 'Finish' }}
            </button>
            <button
                v-if="state > State.SelectSignature"
                @click="backButton"
            >
                Back
            </button>
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </div>
</template>
