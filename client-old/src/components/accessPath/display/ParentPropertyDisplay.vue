<script setup lang="ts">
import { GraphSimpleProperty, GraphComplexProperty, type GraphParentProperty, GraphRootProperty } from '@/types/accessPath/graph';
import { SimpleProperty, ComplexProperty, type ParentProperty } from '@/types/accessPath/basic';
import { computed, ref } from 'vue';
import SimplePropertyDisplay from './SimplePropertyDisplay.vue';
import ButtonIcon from '@/components/common/ButtonIcon.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';
import SignatureDisplay from '@/components/category/SignatureDisplay.vue';

type ParentPropertyDisplayProps = {
    property: GraphParentProperty | ParentProperty;
    isLast?: boolean;
    isRoot?: boolean;
    disableAdditions?: boolean;
};

const props = withDefaults(defineProps<ParentPropertyDisplayProps>(), {
    isLast: true,
    isRoot: true,
    disableAdditions: false,
});

const emit = defineEmits([ 'complex:click', 'simple:click', 'add:click' ]);

const highlighted = ref(false);

const simpleSubpaths = computed(() => {
    return props.property instanceof GraphRootProperty || props.property instanceof GraphComplexProperty ?
        props.property.subpaths.filter((subpath): subpath is GraphSimpleProperty => subpath instanceof GraphSimpleProperty) :
        props.property.subpaths.filter((subpath): subpath is SimpleProperty => subpath instanceof SimpleProperty);
});

const complexSubpaths = computed(() => {
    return props.property instanceof GraphRootProperty || props.property instanceof GraphComplexProperty ?
        props.property.subpaths.filter((subpath): subpath is GraphComplexProperty => subpath instanceof GraphComplexProperty) :
        props.property.subpaths.filter((subpath): subpath is ComplexProperty => subpath instanceof ComplexProperty);
});


function reEmitComplexClick(property: GraphComplexProperty): void {
    emit('complex:click', property);
}

function reEmitSimpleClick(property: GraphSimpleProperty): void {
    emit('simple:click', property);
}

function reEmitAddClick(property: GraphComplexProperty): void {
    emit('add:click', property);
}

function emitComplexClick(): void {
    if (!props.isRoot)
        emit('complex:click', props.property);
}
</script>


<template>
    <div class="d-flex flex-column">
        <div>
            <span
                class="name-text"
                :class="{ highlighted, clickable: !isRoot }"
                @click="emitComplexClick"
                @mouseenter="highlighted = true;"
                @mouseleave="highlighted = false"
            >
                {{ property.name }}:
                <SignatureDisplay
                    v-if="!property.isAuxiliary"
                    :signature="property.signature"
                />
                {
            </span>
        </div>
        <div class="d-flex">
            <div class="filler">
                <div
                    class="filler-line"
                    :class="{ highlighted }"
                />
            </div>
            <div class="d-flex flex-column">
                <SimplePropertyDisplay
                    v-for="(subpath, index) in simpleSubpaths"
                    :key="subpath.name.toString()"
                    :property="subpath"
                    :is-last="index === property.subpaths.length - 1"
                    @simple:click="reEmitSimpleClick"
                />
                <ParentPropertyDisplay
                    v-for="(subpath, index) in complexSubpaths"
                    :key="subpath.name.toString()"
                    :property="subpath"
                    :is-last="index === complexSubpaths.length - 1"
                    :is-root="false"
                    :disable-additions="disableAdditions"
                    @complex:click="reEmitComplexClick"
                    @simple:click="reEmitSimpleClick"
                    @add:click="reEmitAddClick"
                />
                <template v-if="false"> <!-- Hiding the Plus button for now, might use later -->
                    <ButtonIcon
                        v-if="!disableAdditions"
                        class="name-text"
                        @click="emit('add:click', property)"
                        @mouseenter="highlighted = true;"
                        @mouseleave="highlighted = false"
                    >
                        <IconPlusSquare />
                    </ButtonIcon>
                </template>
            </div>
        </div>
        <div>
            <span
                ref="bracketText"
                class="bracket-text"
                :class="{ highlighted, clickable: !isRoot }"
                @click="emitComplexClick"
                @mouseenter="highlighted = true"
                @mouseleave="highlighted = false"
            >
                }{{ isLast ? '' : ',' }}
            </span>
        </div>
    </div>
</template>

<style scoped>
.filler {
    width: 32px;
    padding-left: 3px;
    padding-top: 6px;
    padding-bottom: 6px;
}

.filler-line {
    width: 6px;
    height: 100%;
    border-radius: 3px;
}

.highlighted {
    background-color: var(--color-background-dark);
}

.name-text, .bracket-text {
    width: fit-content;
    padding: 2px 4px;
    border-radius: 4px;
}
</style>
