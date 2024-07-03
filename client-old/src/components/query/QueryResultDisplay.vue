<script setup lang="ts">
import { computed } from 'vue';
import TextArea from '@/components/input/TextArea.vue';
import type { QueryResult } from '@/utils/api/routes/queries';

type QueryResultDisplayProps = {
    result: QueryResult;
    isExecuting?: boolean;
};

const props = defineProps<QueryResultDisplayProps>();

const text = computed(() => props.result?.rows.join(',\n') ?? '');
</script>

<template>
    <TextArea
        v-if="result"
        v-model="text"
        class="w-100"
        :class="{ 'opacity-25': isExecuting }"
        readonly
        :disabled="isExecuting"
        :min-rows="1"
    />
</template>
