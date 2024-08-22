<script setup lang="ts">
import { ref } from 'vue';
import { LayoutType } from '@/types/inference/layoutType';

const props = defineProps<{
    layoutType: LayoutType;
}>();

const emit = defineEmits<{
    (e: 'change-layout', layoutType: LayoutType): void;
}>();

const showOptions = ref(false);

const selectedLayout = ref<LayoutType>(props.layoutType);

function toggleOptions() {
    showOptions.value = !showOptions.value;
}

function changeLayout(layoutType: LayoutType) {
    selectedLayout.value = layoutType;
    emit('change-layout', layoutType);
}


</script>

<template>
    <div class="editor">
        <div class="center-button">
            <button @click="toggleOptions">
                {{ showOptions ? 'Hide Layout' : 'Layout' }}
            </button>
        </div>
        <div
            v-if="showOptions"
            class="options"
        >
            <label>
                <input
                    v-model="selectedLayout"
                    type="radio"
                    name="layout"
                    value="FR"
                    @change="changeLayout(LayoutType.FR)"
                />
                Force-Directed
            </label>
            <label>
                <input
                    v-model="selectedLayout"
                    type="radio"
                    name="layout"
                    value="CIRCLE"
                    @change="changeLayout(LayoutType.CIRCLE)"
                />
                Circle Layout
            </label>
            <label>
                <input
                    v-model="selectedLayout"
                    type="radio"
                    name="layout"
                    value="KK"
                    @change="changeLayout(LayoutType.KK)"
                />
                Kamada-Kawai
            </label>
            <label>
                <input
                    v-model="selectedLayout"
                    type="radio"
                    name="layout"
                    value="ISOM"
                    @change="changeLayout(LayoutType.ISOM)"
                />
                ISOM Layout
            </label>
        </div>
    </div>
</template>

<style scoped>
.editor {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.center-button {
    display: flex;
    justify-content: center;
    width: 100%;
}

.options {
    display: flex;
    flex-direction: column;
}
</style>
