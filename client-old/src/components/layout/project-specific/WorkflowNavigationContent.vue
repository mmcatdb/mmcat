<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router';

type Link = {
    pathName: string;
    label: string;
    params: {
        categoryId: string | string[];
    };
};

const route = useRoute();

function defineLink(pathName: string, label: string): Link {
    return { pathName, label, params: {
        categoryId: route.params.categoryId,
    } };
}

const links = [
    defineLink('schemaCategory', 'Schema Category'),
    defineLink('logicalModels', 'Logical Models'),
    defineLink('datasourcesInCategory', 'Datasources'),
];

const steps = [
    'Add something',
    'Change something else',
];

const currentStepIndex = 0;
</script>

<template>
    <div class="d-flex flex-column align-items-center pt-2">
        <div
            v-for="(step, index) in steps"
            :key="step"
            class="d-flex flex-column align-items-center"
        >
            <div :class="[ 'step-icon rounded-circle d-flex align-items-center justify-content-center border border-2', { 'border-primary text-primary': currentStepIndex === index } ]">
                {{ index + 1 }}
            </div>
            <div class="text-center">
                {{ step }}
            </div>
            <div
                v-if="index < steps.length - 1"
                class="step-separator rounded-pill bg-body mt-2 mb-3"
            />
        </div>
    </div>
    <!-- <RouterLink
        v-for="link in links"
        :key="link.pathName"
        :to="{ name: link.pathName }"
    >
        {{ link.label }}
    </RouterLink> -->
</template>

<style scoped>
.step-icon {
    width: 48px;
    height: 48px;
    font-weight: 800;
}

.step-separator {
    width: 4px;
    height: 64px;
}
</style>