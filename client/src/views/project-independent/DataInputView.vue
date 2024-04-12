<script setup lang="ts">
import API from '@/utils/api';
import type { DataInput } from '@/types/dataInput';

import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DataInputDisplay from '@/components/dataInput/DataInputDisplay.vue';
import DataInputEditor from '@/components/dataInput/DataInputEditor.vue';
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toQueryScalar } from '@/utils/router';

const route = useRoute();
const router = useRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? null : parseInt(rawId);

const isEditing = ref(isNew || route.query.state === 'editing');
const categoryId = toQueryScalar(route.query.categoryId);
const returnPath = categoryId
    ? { name: 'dataInputsInCategory', params: { categoryId } }
    : { name: 'dataInputs' };

const dataInput = ref<DataInput>();

const shouldReturnToAllDataInputsAfterEditing = isEditing.value;

async function fetchDataInput() {
    if (isNew || !id)
        return true;

    const result = await API.dataInputs.getDataInput({ id: id });
    if (!result.status)
        return false;

    dataInput.value = result.data;
    return true;
}

function save(newValue: DataInput) {
    if (shouldReturnToAllDataInputsAfterEditing) {
        router.push(returnPath);
        return;
    }

    dataInput.value = newValue;
    isEditing.value = false;
}

function cancel() {
    if (shouldReturnToAllDataInputsAfterEditing) {
        router.push(returnPath);
        return;
    }

    isEditing.value = false;
}
</script>

<template>
    <div>
        <template v-if="isNew">
            <h1>Create new dataInput</h1>
            <div class="data-input mt-3">
                <DataInputEditor
                    @save="save"
                    @cancel="cancel"
                />
            </div>
        </template>
        <template v-else>
            <h1>Data Input</h1>
            <div
                v-if="dataInput"
                class="data-input mt-3"
            >
                <DataInputEditor
                    v-if="isEditing"
                    :dataInput="dataInput"
                    @save="save"
                    @cancel="cancel"
                    @delete="router.push(returnPath)"
                />
                <DataInputDisplay
                    v-else
                    :dataInput="dataInput"
                    @edit="isEditing = true"
                />
            </div>
            <ResourceLoader :loading-function="fetchDataInput" />
            <div class="button-row">
                <button
                    v-if="!isEditing"
                    @click="router.push(returnPath)"
                >
                    Back
                </button>
            </div>
        </template>
    </div>
</template>

<style scoped>
.data-input{
    display: flex;
}
</style>
