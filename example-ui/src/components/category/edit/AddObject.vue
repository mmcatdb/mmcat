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
            label: '',
            iri: '',
            pimIri: '',
            keyIsValid: true
        };
    },
    computed: {
        iriIsAvailable() {
            return this.graph.schemaCategory.iriIsAvailable(this.iri);
        }
    },
    methods: {
        save() {
            if (this.iri) {
                const object = this.graph.schemaCategory.createObjectWithIri(this.label, undefined, this.iri, this.pimIri);
                if (!object)
                    return;

                this.graph.createNode(object, 'new');
            }
            else {
                const object = this.graph.schemaCategory.createObject(this.label);
                this.graph.createNode(object, 'new');
            }

            this.graph.layout();
            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        }
    }
});
</script>

<template>
    <div>
        <h2>Add Schema Object</h2>
        <table>
            <tr>
                <td class="label">
                    Label:
                </td>
                <td class="value">
                    <input
                        v-model="label"
                    />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Iri?:
                </td>
                <td class="value">
                    <input
                        v-model="iri"
                    />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Pim Iri?:
                </td>
                <td class="value">
                    <input
                        v-model="pimIri"
                    />
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                :disabled="!keyIsValid || !label || !iriIsAvailable"
                @click="save"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>
