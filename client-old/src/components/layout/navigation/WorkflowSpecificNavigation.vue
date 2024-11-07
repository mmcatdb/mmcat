<script setup lang="ts">
import { type InferenceWorkflowStep, inferenceWorkflowSteps } from '@/types/workflow';
import { useWorkflow } from '@/utils/injects';

const workflow = useWorkflow();

const stepLabels: Record<InferenceWorkflowStep, string> = {
    selectInputs: 'Select Input',
    editCategory: 'Edit Schema',
    addMappings: 'Add Mappings',
    finish: 'Get Results',
};

const steps = inferenceWorkflowSteps;
</script>

<template>
    <div class="d-flex flex-column align-items-center pt-2">
        <div
            v-for="(step, index) in steps"
            :key="step"
            class="d-flex flex-column align-items-center"
        >
            <div :class="[ 'step-icon rounded-circle d-flex align-items-center justify-content-center border border-2', { 'border-primary text-primary': step === workflow?.data.step } ]">
                {{ index + 1 }}
            </div>
            <div class="text-center">
                {{ stepLabels[step] }}
            </div>
            <div
                v-if="index < steps.length - 1"
                class="step-separator rounded-pill bg-body mt-2 mb-3"
            />
        </div>
    </div>
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