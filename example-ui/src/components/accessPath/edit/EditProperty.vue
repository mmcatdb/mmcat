<script lang="ts">
import { SimpleProperty, ComplexProperty, type ChildProperty } from '@/types/accessPath/graph';
import { PropertyType, type Graph } from '@/types/categoryGraph';
import type { Name } from '@/types/identifiers';
import { defineComponent } from 'vue';
import type { Database } from '@/types/database';
import type { SchemaObject } from '@/types/schema';

import SignatureInput from '../input/SignatureInput.vue';
import NameInput from '../input/NameInput.vue';
import SchemaIds from '@/components/category/SchemaIds.vue';

enum State {
    SelectSignature,
    SelectType,
    SelectName
}

export default defineComponent({
    components: {
        SignatureInput,
        NameInput,
        SchemaIds
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
        property: {
            type: Object as () => ChildProperty,
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
            State
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
        propertyToType(property: ChildProperty): PropertyType {
            return property instanceof SimpleProperty ? PropertyType.Simple : PropertyType.Complex;
        },
        save() {
            const subpaths = !this.signatureChanged && !this.typeChanged && this.property instanceof ComplexProperty ? this.property.subpaths : [];
            const newProperty = this.type === PropertyType.Simple
                ? new SimpleProperty(this.name, this.signature, this.property.parent)
                : new ComplexProperty(this.name, this.signature, this.property.parent, subpaths);

            this.property.parent.updateOrAddSubpath(newProperty, this.property);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirmSignature() {
            const node = this.signature.sequence.lastNode;

            const type = node.determinedPropertyType;
            if (type !== null) {
                this.type = type;
                this.state = State.SelectName;
            }

            this.state = State.SelectType;
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
            // TODO change signature to empty if it's not valid now
            this.save();
        },
        resetName() {
            this.name = this.property.name.copy();
        },
        deleteProperty() {
            this.property.parent.removeSubpath(this.property);
            this.$emit('save');
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Edit property</h2>
        Schema object: {{ schemaObject.label }}
        <br />
        Ids:
        <SchemaIds :schema-object="schemaObject" />
        <button
            @click="deleteProperty"
        >
            Delete property
        </button>
        <br />
        <template v-if="state >= State.SelectType">
            Signature: {{ signature }}
            <br />
        </template>
        <template v-if="state >= State.SelectName">
            Type: {{ type }}
            <br />
        </template>
        <template v-if="state === State.SelectType">
            Type:<br />
            <input
                id="simple"
                v-model="type"
                type="radio"
                :value="PropertyType.Simple"
            />
            <label
                :class="{ selected: type === PropertyType.Simple }"
                for="simple"
            >
                Simple
            </label><br />
            <input
                id="complex"
                v-model="type"
                type="radio"
                :value="PropertyType.Complex"
            />
            <label
                :class="{ selected: type === PropertyType.Complex }"
                for="complex"
            >
                Complex
            </label><br />
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
        <template v-else-if="state === State.SelectName">
            Name: <span class="selected">{{ name }}</span>
            <NameInput
                v-model="name"
                :graph="graph"
                :database="database"
                :root-node="property.parentNode"
            />
            <br />
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
        <template v-else-if="state === State.SelectSignature">
            Signature: <span class="selected">{{ signature }}</span>
            <SignatureInput
                v-model="signature"
                :graph="graph"
                :database="database"
            />
            <br />
            <button
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
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>

<style scoped>
.selected {
    font-weight: bold;
}
</style>

