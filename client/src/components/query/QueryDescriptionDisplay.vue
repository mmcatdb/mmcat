<script setup lang="ts">
import TextArea from '@/components/input/TextArea.vue';
import type { QueryDescription } from '@/types/query';

type QueryDescriptionDisplayProps = {
    description: QueryDescription;
    isExecuting?: boolean;
};

defineProps<QueryDescriptionDisplayProps>();
</script>

<template>
    <div
        class="d-flex flex-column gap-3"
        :class="{ 'opacity-25': isExecuting }"
    >
        <div
            v-for="part in description.parts"
            :key="part.database.id"
        >
            <div class="pb-1 fw-semibold">
                {{ part.database.label }}:
            </div>
            <TextArea
                v-model="part.query.stringContent"
                class="w-100"
                readonly
                :disabled="isExecuting"
                :min-rows="1"
            />
        </div>
    </div>
</template>
