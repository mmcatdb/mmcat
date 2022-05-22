<script lang="ts">
import { SimpleProperty, ComplexProperty, SequenceSignature, type ParentProperty } from '@/types/accessPath/graph';
import { PropertyType, type Graph } from '@/types/categoryGraph';
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
            type: PropertyType.Simple,
            PropertyType,
            signature: SequenceSignature.empty(this.parentProperty.node),
            name: StaticName.fromString('') as Name,
            state: State.SelectSignature,
            State
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

            const type = this.database.configuration.isComplexPropertyAllowed ?
                node.determinedPropertyType :
                PropertyType.Simple;

            if (type !== null) {
                this.type = type;
                this.state = State.SelectName;
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
        <table>
            <template v-if="state >= State.SelectType">
                <tr>
                    <td class="label">
                        Signature:
                    </td>
                    <td class="value">
                        {{ signature }}
                    </td>
                </tr>
            </template>
            <template v-if="state >= State.SelectName">
                <tr>
                    <td class="label">
                        Type:
                    </td>
                    <td class="value">
                        {{ type }}
                    </td>
                </tr>
            </template>
            <template v-if="state === State.SelectType">
                <tr>
                    <td class="label">
                        Type:
                    </td>
                    <td class="value">
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
                    </td>
                </tr>
                <button
                    @click="confirmType"
                >
                    Confirm
                </button>
            </template>
            <template v-else-if="state === State.SelectName">
                <tr>
                    <td class="label">
                        Name:
                    </td>
                    <td class="value">
                        {{ name }}
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <NameInput
                            v-model="name"
                            :graph="graph"
                            :database="database"
                            :root-node="parentProperty.node"
                        />
                    </td>
                </tr>
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
                    :constraint="database.configuration"
                />
                <br />
                <button
                    @click="confirmSignature"
                >
                    Confirm
                </button>
            </template>
        </table>
        <div class="button-row">
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

