<script lang="ts">
import type { ComplexProperty } from '@/types/accessPath';
import type { NodeSchemaData } from '@/types/categoryGraph';
import { Signature, type Name } from '@/types/identifiers';
import type { Core } from 'cytoscape';
import { defineComponent } from 'vue';
import SignatureInput from '../input/SignatureInput.vue';
import NameInput from '../input/NameInput.vue';

enum State {
    SelectName,
    SelectSignature
}

export default defineComponent({
    components: { SignatureInput, NameInput },
    props: {
        cytoscape: {
            type: Object as () => Core,
            required: true
        },
        propertyNode: {
            type: Object as () => NodeSchemaData,
            required: true
        },
        property: {
            type: Object as () => ComplexProperty,
            required: true
        },
        isNew: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            newSignature: Signature.empty,
            newName: this.property.name.copy(),
            state: State.SelectName,
            State: State
        };
    },
    computed: {
        nameChanged(): boolean {
            return !this.property.name.equals(this.newName as Name);
        },
        signatureChanged(): boolean {
            return !this.property.signature.equals(this.newSignature as Signature);
        }
    },
    methods: {
        save() {
            this.property.update(this.newName as Name, this.newSignature as Signature);
            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirmNewName() {
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
        <h2>Edit complex property</h2>
        <template v-if="state === State.SelectName">
            Name: <span class="value">{{ newName }}</span>
            <NameInput
                v-model="newName"
                :cytoscape="cytoscape"
                :root-node="propertyNode"
            />

                TODO

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
        <template v-else>
            Signature: <span class="value">{{ newSignature }}</span>
            <SignatureInput
                v-model="newSignature"
                :cytoscape="cytoscape"
                :root-node="propertyNode"
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
.outer {
    padding: 16px;
    margin: 16px;
    border: 1px solid white;
}

.value {
    font-weight: bold;
}
</style>

