<script lang="ts">
import { defineComponent } from 'vue';
import type { Job } from '@/types/job';
import { POST } from '@/utils/backendAPI';

import AccessPathJsonEditor from '@/components/job/AccessPathJsonEditor.vue';

export default defineComponent({
    components: {
        AccessPathJsonEditor
    },
    props: {},
    data() {
        return {
            buttonDisabled: false,
            accessPath:
`{
    number : 19,
    nested : 4 {
        propertyA : 5,
        propertyB : 6,
        propertyC : 7
    }
}`
        };
    },
    methods: {
        async createNewJob() {
            this.buttonDisabled = true;
            console.log('New job is being created: ', this.accessPath);

            //const result = POST<Job>('/jobs', { accessPath: this.accessPath });
            const result = await POST<Job>('/jobs', { accessPath: this.accessPath });
            console.log(result);

            this.buttonDisabled = false;
        }
    }
});
</script>

<template>
    <h1>This is going to be a new job</h1>
    <span style="color: red">TODO remove this later</span>
    <AccessPathJsonEditor v-model="accessPath" />
    <br>
    <button
        :disabled="buttonDisabled || true"
        @click="createNewJob"
    >
        Create job
    </button>
</template>

<style scoped>

</style>
