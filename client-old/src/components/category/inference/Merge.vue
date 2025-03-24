<script setup lang="ts">
import { ref } from 'vue';
import type { Graph, Node } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import type { Candidates, ReferenceCandidate,  PrimaryKeyCandidate } from '@/types/inference/candidates';
import ReferenceMerge from '@/components/category/inference/ReferenceMerge.vue';
import PrimaryKeyMerge from '@/components/category/inference/PrimaryKeyMerge.vue';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The graph object used for merging operations. */
    graph: Graph;
    /** Candidates for merging, either by reference or primary key. */
    candidates: Candidates;
}>();

/**
 * Emits custom events to the parent component.
 */
const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', payload: Node[] | ReferenceCandidate): void;
    (e: 'confirm-primary-key-merge', nodes: Node[] | PrimaryKeyCandidate): void;
}>();

/**
 * Tracks the current merge type (either 'reference' or 'primaryKey').
 */
const mergeType = ref<'reference' | 'primaryKey'>('reference');

/**
 * Confirms the reference merge and emits the 'confirm-reference-merge' event.
 */
function confirmReference(payload: Node[] | ReferenceCandidate ) {
    emit('confirm-reference-merge', payload);
}

/**
 * Confirms the primary key merge and emits the 'confirm-primary-key-merge' event.
 */
function confirmPrimaryKey(payload: Node[] | PrimaryKeyCandidate) {
    emit('confirm-primary-key-merge', payload);
}

/**
 * Cancels the current operation by emitting the 'cancel' event.
 */
function cancel() {
    emit('cancel');
}

/**
 * Cancels the current edit by emitting the 'cancel-edit' event.
 */
function cancelEdit() {
    emit('cancel-edit');
}

/**
 * Sets the current merge type to either 'reference' or 'primaryKey'.
 */
function setMergeType(type: 'reference' | 'primaryKey') {
    mergeType.value = type;
}

</script>

<template>
    <div class="merge">
    <h3 class="merge-title">
            Merge Objects
            <span class="tooltip-container">
                <span class="question-mark">?</span>
                <span class="tooltip-text">
                Choose how you want to merge objects in the graph: by reference or by primary key.<br>
                For manual <strong>reference merging</strong>, first select the referencing object, then the referenced object.<br>
                For manual <strong>primary key merging</strong>, first select the object with the primary key, then the object identified by it.<br>
                Alternatively, you can select from precomputed merge candidates in both modes.
                </span>
            </span> 
        </h3>
        <div class="merge-type button-row">
            <button
                :disabled="mergeType === 'reference'"
                @click="setMergeType('reference')"
            >
                Reference
            </button>
            <button
                :disabled="mergeType === 'primaryKey'"
                @click="setMergeType('primaryKey')"
            >
                Primary Key
            </button>
        </div>
        <Divider />
        <ReferenceMerge
            v-if="mergeType === 'reference'"
            :graph="props.graph"
            :candidates="props.candidates"
            @confirm="confirmReference"
            @cancel="cancel"
            @cancel-edit="cancelEdit"
        />
        <PrimaryKeyMerge
            v-else-if="mergeType === 'primaryKey'"
            :graph="props.graph"
            :candidates="props.candidates"
            @confirm="confirmPrimaryKey"
            @cancel="cancel"
            @cancel-edit="cancelEdit"
        />
    </div>
</template>

<style>
.merge-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tooltip-container {
  position: relative;
  display: inline-block;
  flex-shrink: 0;
}

.question-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  font-size: 11px;
  border-radius: 50%;
  background-color: #999;
  color: white;
  font-weight: bold;
  cursor: default;
}

.tooltip-text {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  bottom: 125%;
  left: 50%;
  transform: translateX(-50%);
  width: max-content;
  max-width: 220px;
  padding: 6px 8px;
  font-size: 12px;
  background-color: #333;
  color: #fff;
  text-align: left;
  border-radius: 6px;
  z-index: 1;
  pointer-events: none;
  transition: opacity 0.2s ease-in-out;
}

.tooltip-container:hover .tooltip-text {
  visibility: visible;
  opacity: 1;
}
</style>
