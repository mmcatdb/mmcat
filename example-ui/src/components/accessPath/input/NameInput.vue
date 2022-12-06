<script lang="ts">
import { SequenceSignature } from '@/types/accessPath/graph';
import { type Graph, type Node, createDefaultFilter } from '@/types/categoryGraph';
import type { DatabaseWithConfiguration } from '@/types/database';
import { DynamicName, Signature, StaticName, type Name } from '@/types/identifiers';
import { defineComponent } from 'vue';
import SignatureInput from './SignatureInput.vue';

enum NameType {
    Static,
    Dynamic,
    Anonymous
}

export default defineComponent({
    components: { SignatureInput },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        database: {
            type: Object as () => DatabaseWithConfiguration,
            required: true
        },
        rootNode: {
            type: Object as () => Node,
            required: true
        },
        modelValue: {
            type: Object as () => Name,
            required: true
        },
        disabled: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: this.modelValue,
            type: this.getNameType(this.modelValue),
            staticValue: this.modelValue instanceof StaticName && !this.modelValue.isAnonymous ? this.modelValue.value : '',
            dynamicValue: SequenceSignature.fromSignature(this.modelValue instanceof DynamicName ? this.modelValue.signature : Signature.empty, this.rootNode),
            NameType,
            filter: createDefaultFilter(this.database.configuration)
        };
    },
    watch: {
        modelValue: {
            handler(newValue: Name): void {
                if (!newValue.equals(this.innerValue)) {
                    this.innerValue = this.modelValue;
                    this.type = this.getNameType(this.modelValue);
                    this.staticValue = this.modelValue instanceof StaticName && !this.modelValue.isAnonymous ? this.modelValue.value : '';
                    this.dynamicValue = SequenceSignature.fromSignature(this.modelValue instanceof DynamicName ? this.modelValue.signature : Signature.empty, this.rootNode);
                }
            }
        }
    },
    methods: {
        getNameType(name: Name): NameType {
            return name instanceof StaticName
                ? (name.isAnonymous ? NameType.Anonymous : NameType.Static)
                : NameType.Dynamic;
        },
        updateInnerValue() {
            switch (this.type) {
            case NameType.Static:
                this.innerValue = StaticName.fromString(this.staticValue);
                break;
            case NameType.Dynamic:
                this.innerValue = DynamicName.fromSignature(this.dynamicValue.toSignature());
                break;
            case NameType.Anonymous:
                this.innerValue = StaticName.anonymous;
                break;
            }

            this.$emit('update:modelValue', this.innerValue);
        }
    }
});
</script>

<template>
    <input
        id="static"
        v-model="type"
        type="radio"
        :value="NameType.Static"
        :disabled="disabled"
        @change="updateInnerValue"
    />
    <label
        for="static"
        :class="{ value: type === NameType.Static }"
    >
        Static
    </label>
    <br />
    <input
        v-model="staticValue"
        :disabled="type !== NameType.Static"
        @input="updateInnerValue"
    />
    <br />
    <input
        id="dynamic"
        v-model="type"
        type="radio"
        :value="NameType.Dynamic"
        :disabled="disabled || !database.configuration.isDynamicNamingAllowed"
        @change="updateInnerValue"
    />
    <label
        for="dynamic"
        :class="{ value: type === NameType.Dynamic }"
    >
        Dynamic
    </label>
    <br />
    <span :class="{ disabled: type !== NameType.Dynamic }">
        {{ dynamicValue }}
    </span>
    <br />
    <input
        id="anonymous"
        v-model="type"
        type="radio"
        :value="NameType.Anonymous"
        :disabled="disabled || !database.configuration.isAnonymousNamingAllowed"
        @change="updateInnerValue"
    />
    <label
        for="anonymous"
        :class="{ value: type === NameType.Anonymous }"
    >
        Anonymous
    </label>
    <br />
    <div v-if="type === NameType.Dynamic">
        <SignatureInput
            v-model="dynamicValue"
            :graph="graph"
            :filter="filter"
            :disabled="disabled"
            @input="updateInnerValue"
        />
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

