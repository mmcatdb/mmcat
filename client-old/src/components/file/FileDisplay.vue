<script setup lang="ts">
import { computed, ref } from 'vue';
import API from '@/utils/api';
import { File} from '@/types/file';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';

type FileDisplayProps = {
    file: File;
};

const props = defineProps<FileDisplayProps>();

function stringify(value: unknown): string | undefined {
    if (value === undefined || value === null)
        return undefined;

    if (typeof value === 'object')
        return JSON.stringify(value, null, 4);

    return '' + value;
}

const emit = defineEmits<{
    (e: 'updateFile', file: File): void;
}>();

const fetching = ref(false);

const info = useSchemaCategoryInfo();
/*
async function restartJob() {
    fetching.value = true;
    const result = await API.jobs.createRestartedJob({ id: props.job.id });
    fetching.value = false;
    if (result.status)
        emit('updateJob', Job.fromServer(result.data, info.value));
}*/

const fileTypeText = computed(() => {
    switch (props.file.fileType) {
        case "JSON":
            return "JSON File";
        case "CSV":
            return "CSV File";
        case "DML":
            return "DML Commands";
        default:
            return "Unknown File Type";
    }
});

</script>

<template>
    <div class="border border-primary px-3 py-2">
        <div class="d-flex gap-4 align-items-end">
            <div>
                <div>
                    <strong>{{ fileTypeText }}</strong>
                </div>
                <div class="text-secondary small">
                    {{ file.id }}
                </div>
            </div>
            <div class="col-4 d-flex align-items-center gap-3">
                <div class="text-secondary small text-nowrap monospace-numbers">
                    {{ file.createdAt.toLocaleString(undefined, { month: '2-digit', day: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }) }}
                </div>
                <FixedRouterLink :to="{ name: 'job', params: { id: file.jobId } }">
                    <div class="fs-6">
                        <span class="fw-bold">{{ file.label }}</span>
                    </div>
                </FixedRouterLink>
            </div>
            <div class="d-flex ms-auto align-self-center">
                <button
                    :disabled="fetching"
                    class="info"
                    @click="restartJob"
                >
                    Restart
                </button>
            </div>
        </div>
    </div>
</template>