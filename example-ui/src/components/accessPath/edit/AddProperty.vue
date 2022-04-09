<script lang="ts">
import { SimpleProperty, ComplexProperty, type ChildProperty, SequenceSignature, type ParentProperty } from '@/types/accessPath/graph';
import type { Graph } from '@/types/categoryGraph';
import { StaticName, type Name } from '@/types/identifiers';
import { defineComponent } from 'vue';
import SignatureInput from '../input/SignatureInput.vue';
import NameInput from '../input/NameInput.vue';
import type { Database } from '@/types/database';

enum State {
    SelectSignature,
    SelectType,
    SelectName
}

enum PropertyType {
    Simple = 'Simple',
    Complex = 'Complex'
}

export default defineComponent({
    components: {
        SignatureInput, NameInput
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
        parentProperty: {
            type: Object as () => ParentProperty,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            type: this.propertyToType(this.property),
            PropertyType,
            signature: SequenceSignature.empty(this.parentProperty.node),
            name: StaticName.fromString('') as Name,
            state: State.SelectSignature,
            State
        };
    },
    methods: {
        propertyToType(property: ChildProperty): PropertyType {
            return property instanceof SimpleProperty ? PropertyType.Simple : PropertyType.Complex;
        },
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
            this.name = StaticName.fromString(node.schemaObject.label);
            if (node.isLeaf) {
                this.type = PropertyType.Simple;
                this.state = State.SelectName;
                return;
            }

            if (node.schemaObject.hasComplexId) {
                this.type = PropertyType.Complex;
                this.state = State.SelectName;
                return;
            }

            this.state = State.SelectType;
        },
        confirmType() {
            this.state = State.SelectName;
        },
        confirmName() {
            // TODO change signature to empty if it's not valid now
            this.save();
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Add property</h2>
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
                :class="{ value: type === PropertyType.Simple }"
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
                :class="{ value: type === PropertyType.Complex }"
                for="complex"
            >
                Complex
            </label><br />
            <button
                @click="confirmType"
            >
                Confirm
            </button>
        </template>
        <template v-else-if="state === State.SelectName">
            Name: <span class="value">{{ name }}</span>
            <NameInput
                v-model="name"
                :graph="graph"
                :database="database"
                :root-node="parentProperty.node"
            />
            <br />
            <button
                @click="confirmName"
            >
                Confirm
            </button>
        </template>
        <template v-else-if="state === State.SelectSignature">
            Signature: <span class="value">{{ signature }}</span>
            <SignatureInput
                v-model="signature"
                :graph="graph"
                :database="database"
            />
            <br />
            <button
                @click="confirmSignature"
            >
                Confirm
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

