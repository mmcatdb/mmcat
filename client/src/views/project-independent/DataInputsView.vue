<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { DataInput } from '@/types/dataInput';

import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DataInputDisplay from '@/components/dataInput/DataInputDisplay.vue';
import { useRouter } from 'vue-router';

const dataInputs = ref<DataInput[]>();

async function fetchDataInputs() {
    const result = await API.dataInputs.getAllDataInputs({});
    if (!result.status)
        return false;

    dataInputs.value = result.data;
    return true;
}

const router = useRouter();

function createNew() {
    router.push({ name: 'dataInput', params: { id: 'new' } });
}
</script>

<template>
    <div>
        <h1>Data Inputs</h1>
        <div class="data-inputs mt-3">
            <div
                v-for="dataInput in dataInputs"
                :key="dataInput.id"
            >
                <DataInputDisplay
                    :dataInput="dataInput"
                    @edit="$router.push({ name: 'dataInput', params: { id: dataInput.id }, query: { state: 'editing' } });"
                />
            </div>
        </div>
        <template v-if="dataInputs">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchDataInputs" />
    </div>
</template>

<style scoped>
.data-inputs {
    display: flex;
    flex-wrap: wrap;
}
</style>
