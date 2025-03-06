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

const fetching = ref(false);

const info = useSchemaCategoryInfo();

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

async function downloadFile() {
    fetching.value = true;

    const result = await API.files.downloadFile({ id: props.file.id });
    // For some reason, even though I am sending a ResponseEntity with custom headers from the server,
    // I receive an Object with fields: status and data.
    const fileContent = result.data;
    const { extension, mimeType } = getFileType(props.file.fileType);
    const blob = new Blob([fileContent], { type: mimeType});
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${props.file.id}.${extension}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    fetching.value = false;
}

function getFileType(fileType: string) {
    const fileTypes = {
        JSON: { extension: "json", mimeType: "application/json" },
        CSV: { extension: "csv", mimeType: "text/csv" },
        DML: { extension: "txt", mimeType: "text/plain" }
    };

    return fileTypes[fileType] || { extension: "txt", mimeType: "text/plain" };
}

async function executeDML() {
    fetching.value = true;
    const result = await API.files.executeDML({ id: props.file.id });
    fetching.value = false;
}

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
            <div class="d-flex ms-auto align-self-center gap-1">
                <button
                    :disabled="fetching"
                    class="info"
                    @click="downloadFile"
                >
                    Download
                </button>
                <button
                    v-if="file.fileType === 'DML'"
                    :disabled="fetching"
                    class="info"
                    @click="executeDML"
                >
                    Execute
                </button>
            </div>
        </div>
    </div>
</template>