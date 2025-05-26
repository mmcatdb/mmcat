<script setup lang="ts">
import type { ObjexIds } from '@/types/identifiers';
import IconMinusSquare from '@/components/icons/IconMinusSquare.vue';
import ButtonIcon from '@/components/common/ButtonIcon.vue';
import SignatureIdDisplay from './SignatureIdDisplay.vue';

type ObjexIdsDisplayProps = {
    ids: ObjexIds;
    disabled?: boolean;
};

defineProps<ObjexIdsDisplayProps>();

const emit = defineEmits([ 'deleteNonSignature', 'deleteSignature' ]);
</script>

<template>
    <div class="d-flex flex-column">
        <template v-if="ids.isSignatures">
            <div
                v-for="(signatureId, idIndex) in ids.signatureIds"
                :key="idIndex"
            >
                <SignatureIdDisplay :signature-id="signatureId" />
                <ButtonIcon
                    v-if="!disabled"
                    class="ms-2 button-icon-error"
                    @click="() => emit('deleteSignature', idIndex)"
                >
                    <IconMinusSquare />
                </ButtonIcon>
            </div>
        </template>
        <template v-else>
            <div>
                <span
                    class="signature-span monospace-font"
                >
                    {{ ids.type }}
                </span>
                <ButtonIcon
                    v-if="!disabled"
                    class="ms-2 button-icon-error"
                    @click="() => emit('deleteNonSignature')"
                >
                    <IconMinusSquare />
                </ButtonIcon>
            </div>
        </template>
    </div>
</template>
