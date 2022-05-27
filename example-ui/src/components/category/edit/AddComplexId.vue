<script lang="ts">
import { type Graph, type NodeNeighbour, type Node, FilterType } from '@/types/categoryGraph';
import { SchemaIdFactory } from '@/types/identifiers';
import { defineComponent } from 'vue';
import { SequenceSignature } from '@/types/accessPath/graph';
import { Cardinality } from "@/types/schema";
import SchemaId from '../SchemaId.vue';
import SignatureInput from '../../accessPath/input/SignatureInput.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';

export default defineComponent({
    components: {
        SchemaId,
        SignatureInput,
        IconPlusSquare
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        node: {
            type: Object as () => Node,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            schemaIdFactory: new SchemaIdFactory(),
            addingSignature: false,
            signature: SequenceSignature.empty(this.node),
            idIsNotEmpty: false,
            filter: {
                type: FilterType.Composite,
                function: (neighbour: NodeNeighbour) => {
                    return neighbour.morphism.min === Cardinality.One
                        && neighbour.morphism.max === Cardinality.One
                        && neighbour.dualMorphism.max === Cardinality.Star;
                }
            }
        };
    },
    methods: {
        save() {
            this.node.addId(this.schemaIdFactory.schemaId);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        startAddingSignature() {
            this.signature = SequenceSignature.empty(this.node);
            this.addingSignature = true;
            this.idIsNotEmpty = false;
        },
        cancelAddingSignature() {
            this.addingSignature = false;
        },
        addSignature() {
            this.schemaIdFactory.addSignature(this.signature.toSignature());
            this.addingSignature = false;
            this.idIsNotEmpty = true;
        }
    }
});
</script>

<template>
    <h2>Add complex Id</h2>
    <table>
        <tr>
            <td class="label">
                Id:
            </td>
            <td class="value fix-icon-height">
                <SchemaId :schema-id="schemaIdFactory.schemaId" />
                <span
                    v-if="!addingSignature"
                    class="button-icon"
                    :class="{ 'ml-2': !schemaIdFactory.isEmpty }"
                    @click="startAddingSignature"
                >
                    <IconPlusSquare />
                </span>
            </td>
        </tr>
    </table>
    <div
        v-if="addingSignature"
        class="editor"
    >
        <h2>Add signature</h2>
        <table>
            <tr>
                <td class="label">
                    Signature:
                </td>
                <td class="value">
                    {{ signature }}
                </td>
            </tr>
        </table>
        <SignatureInput
            v-model="signature"
            :graph="graph"
            :filters="filter"
        />
        <div class="button-row">
            <button
                :disabled="signature.isEmpty"
                @click="addSignature"
            >
                Confirm
            </button>
            <button @click="cancelAddingSignature">
                Cancel
            </button>
        </div>
    </div>
    <div class="button-row">
        <button
            :disabled="schemaIdFactory.length <= 1"
            @click="save"
        >
            Confirm
        </button>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>

<style scoped>
.signature-span {
    background-color: var(--color-primary-dark);
    border-radius: 4px;
    padding: 0px 6px 0px 4px;
    font-weight: bold;
}

.comma-span {
    margin-right: 8px;
    margin-left: 2px;
}

.fix-icon-height {
    display: inline-flex;
}

.fix-icon-height > .button-icon {
    max-height: 20px;
}

.fix-icon-height svg.icon {
    top: 2px;
}

.ml-2 {
    margin-left: 8px;
}
</style>

