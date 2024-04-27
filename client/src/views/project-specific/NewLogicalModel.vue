<script setup lang="ts">
import { onMounted, ref } from 'vue';
import API from '@/utils/api';
import { useRouter } from 'vue-router';
import { useSchemaCategoryId } from '@/utils/injects';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { Datasource } from '@/types/datasource';

const datasources = ref<Datasource[]>();
const selectedDatasource = ref<Datasource>();
const label = ref('');
const fetching = ref(false);

onMounted(async () => {
    const result = await API.datasources.getAllDatasources({});
    if (result.status)
        datasources.value = result.data.map(Datasource.fromServer);
});

const router = useRouter();

const categoryId = useSchemaCategoryId();

async function createLogicalModel() {
    if (!selectedDatasource.value || !label.value)
        return;

    fetching.value = true;

    const result = await API.logicalModels.createNewLogicalModel({}, {
        datasourceId: selectedDatasource.value.id,
        categoryId,
        label: label.value,
    });
    if (result.status)
        router.push({ name: 'logicalModels' });

    fetching.value = false;
}
</script>

<template>
    <div>
        <h1>Create a new logical model</h1>
        <ValueContainer>
            <ValueRow label="Datasource:">
                <select v-model="selectedDatasource">
                    <option
                        v-for="datasource in datasources"
                        :key="datasource.id"
                        :value="datasource"
                    >
                        {{ datasource.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow label="Label:">
                <input v-model="label" />
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="fetching || !selectedDatasource || !label"
                @click="createLogicalModel"
            >
                Create logical model
            </button>
        </div>
    </div>
</template>
