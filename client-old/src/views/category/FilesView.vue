<script setup lang="ts">
import { ref } from 'vue';
import { File } from '@/types/file';
import API from '@/utils/api';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import FileDisplay from '@/components/file/FileDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';

const files = ref<File[]>();

const info = useSchemaCategoryInfo();

async function fetchFiles() {
    const result = await API.files.getAllFilesInCategory({ categoryId: info.value.id });
    if (!result.status)
        return false;

    files.value = result.data.map(file => File.fromServer(file, info.value)).sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
    return true;
}
</script>

<template>
    <div>
        <h1>Files</h1>
        <div
            v-if="files"
            class="d-flex flex-column gap-3"
        >
            <div
                v-for="file in files"
                :key="file.id"
            >
                <FileDisplay
                    :file="file"
                />
            </div>
        </div>
        <ResourceLoader
            :loading-function="fetchFiles"
            :refresh-period="2000"
        />
    </div>
</template>
