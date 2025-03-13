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

const editingLabel = ref(false);
const editedLabel = ref(props.file.label);

const editingDescription = ref(false);
const editedDescription = ref(props.file.description);

const showDetails = ref(false);

async function saveLabel() {
    if (editedLabel.value.trim() === props.file.label) {
        editingLabel.value = false;
        return;
    }

    const result = await API.files.updateFile({ id: props.file.id }, { value: editedLabel.value.trim(), isLabel: true })
    editingLabel.value = false;
}

async function saveDescription() {
    if (editedDescription.value.trim() === props.file.description) {
        editingDescription.value = false;
        return;
    }

    const result = await API.files.updateFile({ id: props.file.id }, { value: editedDescription.value.trim(), isLabel: false })
    editingDescription.value = false;
}

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
    <div class="border border-primary px-3 py-2 position-relative">
        <div class="d-flex gap-4 align-items-end">
            <div>
                <div class="d-flex align-items-center gap-2">
                    <strong v-if="!editing" @click="editing = true" class="editable">
                        {{ file.label }}
                    </strong>
                    <input
                        v-else
                        v-model="editedLabel"
                        @blur="saveLabel"
                        @keyup.enter="saveLabel"
                        class="form-control form-control-sm"
                        autofocus
                    />
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
                        <span class="fw-bold">{{ file.jobLabel }}</span>
                    </div>
                </FixedRouterLink>
            </div>
            <div class="d-flex ms-auto align-self-center gap-1">
                <button
                    :disabled="fetching"
                    class="info"
                    @click="showDetails = !showDetails"
                >
                    Details
                </button>
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
        <transition name="slide">
            <div v-if="showDetails" class="details-panel">
                <hr class="separator" />
                <p><strong>Name:</strong> {{ file.label }}</p>
                <p><strong>Id:</strong> {{ file.id }}</p>
                <p><strong>Date of creation:</strong> {{ file.createdAt.toLocaleString() }}</p>
                <p><strong>File Type:</strong> {{ file.fileType }}</p>
                <p>
                    <strong>Description:</strong>
                    <span v-if="!editingDescription" @click="editingDescription = true" class="editable">
                        {{ file.description || "Click to add a description" }}
                    </span>
                    <textarea
                        v-else
                        v-model="editedDescription"
                        @blur="saveDescription"
                        @keyup.enter="saveDescription"
                        class="form-control form-control-sm"
                        rows="2"
                        autofocus
                    ></textarea>
                </p>

                <button class="close-btn" @click="showDetails = false">Close</button>
            </div>
        </transition>
    </div>
</template>

<style scoped>
.editable {
    cursor: pointer;
    border-bottom: 1px dashed #007bff;
    transition: color 0.2s;
}

.editable:hover {
    color: #007bff;
}

.details-panel {
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    background: #f8f9fa;
    border: 1px solid #ddd;
    padding: 15px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease-in-out;
    z-index: 10;
}

.separator {
    border: none;
    height: 1px;
    background: #ccc;
    margin: 10px 0;
}

.slide-enter-active, .slide-leave-active {
    transition: transform 0.3s ease-in-out, opacity 0.3s;
}

.slide-enter-from, .slide-leave-to {
    transform: translateY(-20px);
    opacity: 0;
}

.close-btn {
    background: #dc3545;
    color: white;
    border: none;
    padding: 5px 10px;
    cursor: pointer;
    border-radius: 5px;
    float: right;
}

.close-btn:hover {
    background: #c82333;
}

</style>
