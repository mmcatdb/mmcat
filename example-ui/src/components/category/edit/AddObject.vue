<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
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
        return {
            label: ''
        };
    },
    methods: {
        save() {
            // TODO
            const todo = 1111;
            const object = this.graph.schemaCategory.createObject(this.label, todo);
            this.graph.createNode(object);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirm() {
            this.save();
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
        <button
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

