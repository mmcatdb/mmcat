<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { File } from '@/types/file';
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

const showExecutionPrompt = ref(false);

const newDatabaseName = ref('');

async function saveLabel() {
    const newLabel = editedLabel.value.trim();
    if (newLabel === props.file.label) {
        editingLabel.value = false;
        return;
    }

    const result = await API.files.updateFile({ id: props.file.id }, { label: newLabel });
    editingLabel.value = false;
}

async function saveDescription() {
    const newDescription = editedDescription.value.trim();
    if (newDescription === props.file.description) {
        editingDescription.value = false;
        return;
    }

    const result = await API.files.updateFile({ id: props.file.id }, { description: newDescription });
    editingDescription.value = false;
}

async function downloadFile() {
    fetching.value = true;

    const result = await API.files.downloadFile({ id: props.file.id });
    // For some reason, even though I am sending a ResponseEntity with custom headers from the server,
    // I receive an Object with fields: status and data.
    if (!result.status) 
        throw new Error('Download failed');

    const fileContent = result.data;
    const { extension, mimeType } = getFileType(props.file.fileType);
    triggerDownload(fileContent, `${props.file.id}.${extension}`, mimeType);

    fetching.value = false;
}

async function downloadMetadata() {
    const metadata = {
        name: props.file.label,
        id: props.file.id,
        type: props.file.fileType,
        dateCreated: props.file.createdAt.toLocaleString(),
        datesExecuted: props.file.executedAt?.map(date => date.toLocaleString()) || [],
        description: props.file.description,
    };

    const jsonString = JSON.stringify(metadata, null, 2);
    triggerDownload(jsonString, `${props.file.id}.metadata.json`, 'application/json');
}

function triggerDownload(content: string | Blob, filename: string, mimeType: string) {
    const blob = new Blob([ content ], { type: mimeType });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

const fileTypes: Record<string, { extension: string, mimeType: string }> = {
    JSON: { extension: 'json', mimeType: 'application/json' },
    CSV: { extension: 'csv', mimeType: 'text/csv' },
    DML: { extension: 'txt', mimeType: 'text/plain' },
};

function getFileType(fileType: string) {

    return fileTypes[fileType] ?? { extension: 'txt', mimeType: 'text/plain' };
}

async function executeDML(mode: string, newDBName?: string) {
    if (props.file.executedAt?.length && !showExecutionPrompt.value) {
        showExecutionPrompt.value = true;
        return;
    }

    fetching.value = true;
    const result = await API.files.executeDML({ id: props.file.id }, { mode: mode, newDBName: newDBName });
    showExecutionPrompt.value = false;
    fetching.value = false;    
}

</script>

<template>
    <div class="border border-primary px-3 py-2 position-relative">
        <div class="d-flex gap-4 align-items-end">
            <div>
                <div class="d-flex align-items-center gap-2">
                    <strong
                        v-if="!editingLabel"
                        class="editable"
                        @click="editingLabel = true"
                    >
                        {{ file.label }}
                    </strong>
                    <input
                        v-else
                        v-model="editedLabel"
                        class="form-control form-control-sm"
                        autofocus
                        @blur="saveLabel"
                        @keyup.enter="saveLabel"
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
                    @click="executeDML('execute')"
                >
                    Execute
                </button>
            </div>
        </div>
        <transition name="slide">
            <div
                v-if="showDetails"
                class="details-panel"
            >
                <hr class="separator" />
                <p><strong>Name:</strong> {{ file.label }}</p>
                <p><strong>Id:</strong> {{ file.id }}</p>
                <p><strong>Type:</strong> {{ file.fileType }}</p>
                <p><strong>Date of creation:</strong> {{ file.createdAt.toLocaleString() }}</p>
                <p v-if="file.fileType === 'DML'">
                    <strong>Dates of executions:</strong> 
                    {{ file.executedAt?.length ? file.executedAt.map(date => date.toLocaleString()).join(', ') : 'No executions' }}
                </p>
                <p>
                    <strong>Description: </strong>
                    <span
                        v-if="!editingDescription"
                        class="editable"
                        @click="editingDescription = true"
                    >
                        {{ file.description || "Click to add a description" }}
                    </span>
                    <textarea
                        v-else
                        v-model="editedDescription"
                        class="form-control form-control-sm"
                        rows="2"
                        autofocus
                        @blur="saveDescription"
                        @keyup.enter="saveDescription"
                    />
                </p>
                <div class="details-footer">
                    <button @click="downloadMetadata">
                        Download Details
                    </button>
                </div>
            </div>
        </transition>
        <transition name="fade">
            <div
                v-if="showExecutionPrompt"
                class="overlay"
            />
        </transition>
        <transition name="fade">
            <div
                v-if="showExecutionPrompt"
                class="execution-prompt"
            >
                <h3>Execution Options</h3>
                <p>This file has been executed before. What would you like to do?</p>
                <div class="options">
                    <div class="option">
                        <h4>Overwrite Data</h4>
                        <p>Delete the existing dataset and replace it with the new execution.</p>
                        <button
                            class="info"
                            @click="executeDML('delete_and_execute')"
                        >
                            Overwrite
                        </button>
                    </div>
                    <div class="option">
                        <h4>Create New Database</h4>
                        <p>Execute commands in a new database without affecting existing data.</p>
                        <input 
                            v-model="newDatabaseName" 
                            placeholder="Enter new database name" 
                            class="form-control"
                        />
                        <button 
                            class="info mt-2" 
                            :disabled="!newDatabaseName.trim()"
                            @click="executeDML('create_new_and_execute', newDatabaseName)"
                        >
                            New Database
                        </button>
                    </div>
                </div>
                <button
                    class="cancel"
                    @click="showExecutionPrompt = false"
                >
                    Cancel
                </button>
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

.details-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
}

.overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
}

.execution-prompt {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: white;
    padding: 25px;
    border-radius: 10px;
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
    z-index: 1000;
    width: 400px;
    text-align: center;
}

.execution-prompt h3 {
    margin-bottom: 15px;
    font-size: 1.5rem;
    font-weight: bold;
    color: #007bff;
}

.options {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.option {
    padding: 10px;
    border-radius: 6px;
    background: #f8f9fa;
    text-align: left;
    border-left: 5px solid #007bff;
}

.option h4 {
    margin: 0;
    font-size: 1.2rem;
}

.option p {
    font-size: 0.9rem;
    color: #555;
}

.option input {
    width: 100%;
    margin-bottom: 10px;
}

</style>
