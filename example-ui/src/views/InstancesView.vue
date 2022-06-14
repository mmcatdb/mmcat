<script lang="ts">
import { defineComponent } from 'vue';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading
    },
    data() {
        return {
            instances: null as string[] | null,
            fetched: false
        };
    },
    async mounted() {
        const result = await GET<string[]>('/instances');
        if (result.status)
            this.instances = [ ...result.data ];

        this.fetched = true;
    }
});
</script>

<template>
    <div>
        <h1>Instances</h1>
        <div
            v-if="instances"
            class="instances"
        >
            <div
                v-for="(instance, index) in instances"
                :key="index"
            >
                {{ instance }}
            </div>
        </div>
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.instances {
    display: flex;
}
</style>
