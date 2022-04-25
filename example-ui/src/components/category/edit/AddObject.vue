<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { Key } from '@/types/identifiers';
import { defineComponent } from 'vue';

export default defineComponent({
    components: {

    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        const key = this.graph.schemaCategory.suggestKey();
        return {
            label: '',
            key,
            keyValue: key.value,
            keyIsValid: true
        };
    },
    methods: {
        save() {
            // TODO
            const object = this.graph.schemaCategory.createObject(this.label, this.key, []);
            this.graph.createNode(object);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirm() {
            this.save();
        },
        keyValueChanged() {
            this.key = Key.createNew(this.keyValue);
            this.keyIsValid = this.graph.schemaCategory.isKeyAvailable(this.key);
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Add Schema Object</h2>
        Label:
        <input
            v-model="label"
        />
        <br />
        Key
        <input
            v-model="keyValue"
            type="number"
            min="0"
            step="1"
            @input="keyValueChanged"
        />
        <br />
        <button
            :disabled="!keyIsValid || !label"
            @click="confirm"
        >
            Confirm
        </button>
        <button
            @click="cancel"
        >
            Cancel
        </button>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

