<script setup lang="ts">
import { computed } from 'vue';
import TextArea from '@/components/input/TextArea.vue';
import type { Result } from '@/types/api/result';
import type { QueryResult } from '@/utils/api/routes/queries';

type QueryResultDisplayProps = {
    result: Result<QueryResult> | undefined;
    isExecuting?: boolean;
};

const props = defineProps<QueryResultDisplayProps>();

const text = computed(() => {
    if (!props.result)
        return '';

    return props.result.status
        ? props.result.data.rows.join(',\n')
        : 'Error :(\nname: ' + props.result.error.name + '\ndata:\n' + props.result.error.data;
});
</script>

<template>
    <TextArea
        v-if="result"
        v-model="text"
        class="w-100"
        :class="{ 'text-danger': !result.status, 'opacity-25': isExecuting }"
        readonly
        :disabled="isExecuting"
    />
</template>
