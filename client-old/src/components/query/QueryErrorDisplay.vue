<script setup lang="ts">
import { computed } from 'vue';
import TextArea from '@/components/input/TextArea.vue';

type QueryResultDisplayProps = {
    error: any;
    isExecuting?: boolean;
};

const props = defineProps<QueryResultDisplayProps>();

const text = computed(() => 'Error :(\nname: ' + props.error.name + '\ndata:\n' + props.error.data);
</script>

<template>
    <TextArea
        v-model="text"
        class="w-100 text-danger"
        :class="{ 'opacity-25': isExecuting }"
        readonly
        :disabled="isExecuting"
        :min-rows="1"
    />
</template>
