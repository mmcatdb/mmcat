<script setup lang="ts">
import { ObjectIds, Type } from '@/types/identifiers';
import IconMinusSquare from '@/components/icons/IconMinusSquare.vue';
import ButtonIcon from '@/components/ButtonIcon.vue';
import SignatureIdDisplay from './SignatureIdDisplay.vue';

interface ObjectIdsDisplayProps {
    ids: ObjectIds;
    disabled?: boolean;
}

defineProps<ObjectIdsDisplayProps>();

const emit = defineEmits([ 'deleteNonSignature', 'deleteSignature' ]);
</script>

<template>
    <div class="outer">
        <template v-if="ids.type === Type.Signatures">
            <div
                v-for="(signatureId, idIndex) in ids.signatureIds"
                :key="idIndex"
            >
                <SignatureIdDisplay :signature-id="signatureId" />
                <ButtonIcon
                    v-if="!disabled"
                    class="delete-button button-icon-error"
                    @click="() => emit('deleteSignature', idIndex)"
                >
                    <IconMinusSquare />
                </ButtonIcon>
            </div>
        </template>
        <template v-else>
            <div>
                <span
                    class="signature-span"
                >
                    {{ ids.type }}
                </span>
                <ButtonIcon
                    v-if="!disabled"
                    class="delete-button button-icon-error"
                    @click="() => emit('deleteNonSignature')"
                >
                    <IconMinusSquare />
                </ButtonIcon>
            </div>
        </template>
    </div>
</template>

<style scoped>
.outer {
    display: flex;
    flex-direction: column;
}

.delete-button {
    margin-left: 8px;
}
</style>
