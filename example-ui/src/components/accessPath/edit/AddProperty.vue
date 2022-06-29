<script lang="ts">
import { SimpleProperty, ComplexProperty, SequenceSignature, type ParentProperty } from '@/types/accessPath/graph';
import { PropertyType, type Graph, createDefaultFilter, Node } from '@/types/categoryGraph';
import { StaticName, type Name } from '@/types/identifiers';
import { defineComponent } from 'vue';
import SignatureInput from '../input/SignatureInput.vue';
import TypeInput from '../input/TypeInput.vue';
import NameInput from '../input/NameInput.vue';
import type { DatabaseView } from '@/types/database';

enum State {
    SelectSignature,
    SelectType,
    SelectName
}

export default defineComponent({
    components: {
        SignatureInput,
        TypeInput,
        NameInput
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
        parentProperty: {
            type: Object as () => ParentProperty,
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
                ? new SimpleProperty(this.name, this.signature, this.parentProperty)
                : new ComplexProperty(this.name, this.signature, this.parentProperty);

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
        <table>
            <tr v-if="state >= State.SelectSignature">
                <td class="label">
                    Signature:
                </td>
                <td class="value">
                    {{ signature }}
                </td>
            </tr>
            <tr v-if="state >= State.SelectName">
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    {{ type }}
                </td>
            </tr>
            <tr v-if="state === State.SelectType">
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    <TypeInput v-model="type" />
                </td>
            </tr>
            <tr v-if="state === State.SelectName">
                <td class="label">
                    Name:
                </td>
                <td class="value">
                    <NameInput
                        v-model="name"
                        :graph="graph"
                        :database="database"
                        :root-node="parentProperty.node"
                    />
                </td>
            </tr>
        </table>
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

<style scoped>

</style>
