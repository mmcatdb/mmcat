<script setup lang="ts">
import { ref } from 'vue';
import { LayoutType } from '@/types/inference/layoutType';

/**
 * Props passed to the component.
 * @typedef {Object} Props
 * @property {LayoutType} layoutType - The current layout type to be displayed and selected.
 */
const props = defineProps<{
    layoutType: LayoutType;
}>();

/**
 * Emits custom events to the parent component.
 * @emits change-layout - Emitted when the user selects a new layout type.
 * @param {LayoutType} layoutType - The selected layout type.
 */
const emit = defineEmits<{
    (e: 'change-layout', layoutType: LayoutType): void;
}>();

/**
 * Reactive variable to toggle the visibility of layout options.
 */
const showOptions = ref(false);

/**
 * Reactive variable to track the currently selected layout type.
 */
const selectedLayout = ref<LayoutType>(props.layoutType);

/**
 * Toggles the visibility of the layout options dropdown.
 */
function toggleOptions() {
    showOptions.value = !showOptions.value;
}

/**
 * Changes the layout type and emits the 'change-layout' event.
 * @param {LayoutType} layoutType - The selected layout type.
 */
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
