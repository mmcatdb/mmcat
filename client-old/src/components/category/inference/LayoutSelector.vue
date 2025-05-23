<script setup lang="ts">
import { ref } from 'vue';
import { LayoutType } from '@/types/inference/layoutType';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The current layout type to be displayed and selected. */
    layoutType: LayoutType;
}>();

/**
 * Emits custom events to the parent component.
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
 */
function changeLayout(layoutType: LayoutType) {
    selectedLayout.value = layoutType;
    emit('change-layout', layoutType);
}

</script>

<template>
    <div
        class="editor w-fit"
        style="min-width: 200px;"
    >
        <div
            v-if="showOptions"
            class="pb-3 d-flex flex-column"
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

        <button
            class="w-100"
            @click="toggleOptions"
        >
            {{ showOptions ? 'Hide Layout' : 'Layout' }}
        </button>
    </div>
</template>
