<script lang="ts">
import { SimpleProperty, ComplexProperty, type ChildProperty } from '@/types/accessPath';
import type { NodeSchemaData } from '@/types/categoryGraph';
import type { Signature, Name } from '@/types/identifiers';
import type { Core } from 'cytoscape';
import { defineComponent } from 'vue';
import SignatureInput from '../input/SignatureInput.vue';
import NameInput from '../input/NameInput.vue';

enum State {
    SelectType,
    SelectName,
    SelectSignature
}

enum PropertyType {
    Simple,
    Complex
}

export default defineComponent({
    components: { SignatureInput, NameInput },
    props: {
        cytoscape: {
            type: Object as () => Core,
            required: true
        },
        parentNode: { // TODO should be parentNode
            type: Object as () => NodeSchemaData,
            required: true
        },
        property: {
            type: Object as () => ChildProperty,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            newType: this.propertyToType(this.property),
            newSignature: this.property.signature.copy() as Signature,
            newName: this.property.name.copy() as Name,
            state: State.SelectType,
            State: State,
            PropertyType: PropertyType
        };
    },
    computed: {
        typeChanged(): boolean {
            return this.newType !== this.propertyToType(this.property);
        },
        nameChanged(): boolean {
            return !this.property.name.equals(this.newName as Name);
        },
        signatureChanged(): boolean {
            return !this.property.signature.equals(this.newSignature as Signature);
        },
        isNew(): boolean {
            return !this.property.parent;
        }
    },
    methods: {
        propertyToType(property: ChildProperty): PropertyType {
            return property instanceof SimpleProperty ? PropertyType.Simple : PropertyType.Complex;
        },
        save() {
            const newProperty = this.newType === PropertyType.Simple ? new SimpleProperty(this.newName, this.newSignature) : new ComplexProperty(this.newName, this.newSignature);

            this.$emit('save', newProperty);
        },
        cancel() {
            this.$emit('cancel');
        },
        confirmNewType() {
            this.state = State.SelectName;
        },
        keepOldType() {
            this.resetType();
            this.confirmNewType();
        },
        resetType() {
            this.newType = this.propertyToType(this.property);
        },
        confirmNewName() {
            // TODO change signature to empty if it's not valid now
            this.state = State.SelectSignature;
        },
        keepOldName() {
            if (this.nameChanged)
                this.resetName();

            this.confirmNewName();
        },
        resetName() {
            this.newName = this.property.name.copy();
        },
        confirmNewSignature() {
            this.save();
        },
        keepOldSignature() {
            if (this.signatureChanged)
                this.resetSignature();

            this.confirmNewSignature();
        },
        resetSignature() {
            this.newSignature = this.property.signature.copy();
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Edit property</h2>
        <template v-if="state === State.SelectType">
            Type:<br />
            <input
                id="simple"
                v-model="newType"
                type="radio"
                :value="PropertyType.Simple"
            />
            <label
                :class="{ value: newType === PropertyType.Simple }"
                for="simple"
            >
                Simple
            </label><br />
            <input
                id="complex"
                v-model="newType"
                type="radio"
                :value="PropertyType.Complex"
            />
            <label
                :class="{ value: newType === PropertyType.Complex }"
                for="complex"
            >
                Complex
            </label><br />
            <button
                :disabled="!(typeChanged || isNew)"
                @click="confirmNewType"
            >
                Confirm
            </button>
            <button
                v-if="!isNew"
                @click="keepOldName"
            >
                Keep current
            </button>
        </template>
        <template v-else-if="state === State.SelectName">
            Name: <span class="value">{{ newName }}</span>
            <NameInput
                v-model="newName"
                :cytoscape="cytoscape"
                :root-node="parentNode"
            />
            <br />
            <button
                :disabled="!(nameChanged || isNew)"
                @click="confirmNewName"
            >
                Confirm
            </button>
            <button
                v-if="!isNew"
                @click="keepOldName"
            >
                Keep current
            </button>
            <button
                :disabled="!nameChanged"
                @click="resetName"
            >
                Reset
            </button>
        </template>
        <template v-else-if="state === State.SelectSignature">
            Signature: <span class="value">{{ newSignature }}</span>
            <SignatureInput
                v-model="newSignature"
                :cytoscape="cytoscape"
                :root-node="parentNode"
            />
            <br />
            <button
                :disabled="!(signatureChanged || isNew)"
                @click="confirmNewSignature"
            >
                Confirm
            </button>
            <button
                v-if="!isNew"
                @click="keepOldSignature"
            >
                Keep current
            </button>
            <button
                :disabled="!signatureChanged"
                @click="resetSignature"
            >
                Reset
            </button>
        </template>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

