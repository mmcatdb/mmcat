<script lang="ts">
import type { RootProperty, ChildProperty } from '@/types/accessPath';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        accessPath: {
            type: Object as () => RootProperty | ChildProperty | null,
            required: true
        }
    },
    data() {
        return {};
    }
});
</script>

<template>
    <div>
        <textarea
            :value="accessPath?.toString()"
            :disabled="true"
            class="accessPathDisplay"
            spellcheck="false"
        />
    </div>
</template>

<style scoped>
.accessPathDisplay {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}
</style>
