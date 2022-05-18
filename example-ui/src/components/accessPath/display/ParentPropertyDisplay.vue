<script lang="ts">
import { SimpleProperty, ComplexProperty, type ParentProperty } from '@/types/accessPath/graph';
import { defineComponent } from 'vue';
import SimplePropertyDisplay from './SimplePropertyDisplay.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';

export default defineComponent({
    name: 'ComplexPropertyDisplay',
    components: {
        SimplePropertyDisplay,
        IconPlusSquare,
    },
    props: {
        property: {
            type: Object as () => ParentProperty,
            required: true
        },
        isLast: {
            type: Boolean,
            required: true
        }
    },
    emits: [ 'complex:click', 'simple:click', 'add:click' ],
    data() {
        return {
            highlighted: false
        };
    },
    computed: {
        simpleSubpaths(): SimpleProperty[] {
            return this.property.subpaths.filter((subpath): subpath is SimpleProperty => subpath instanceof SimpleProperty);
        },
        complexSubpaths(): ComplexProperty[] {
            return this.property.subpaths.filter((subpath): subpath is ComplexProperty => subpath instanceof ComplexProperty);
        }
    },
    methods: {
        reEmitComplexClick(property: ComplexProperty): void {
            this.$emit('complex:click', property);
        },
        reEmitSimpleClick(property: SimpleProperty): void {
            this.$emit('simple:click', property);
        },
        reEmitAddClick(property: ComplexProperty): void {
            this.$emit('add:click', property);
        }
    }
});
</script>


<template>
    <div class="outer">
        <div class="row">
            <span
                class="name-text"
                :class="{ highlighted }"
                @click="$emit('complex:click', property)"
                @mouseenter="highlighted = true;"
                @mouseleave="highlighted = false"
            >
                {{ property.name }}: {{ property.isAuxiliary ? '' : (property.signature + ' ') }}{
            </span>
        </div>
        <div class="divider">
            <div class="filler">
                <div
                    class="filler-line"
                    :class="{ highlighted }"
                />
            </div>
            <div class="inner">
                <SimplePropertyDisplay
                    v-for="(subpath, index) in simpleSubpaths"
                    :key="index"
                    :property="subpath"
                    :is-last="index === property.subpaths.length - 1"
                    @simple:click="reEmitSimpleClick"
                />
                <ComplexPropertyDisplay
                    v-for="(subpath, index) in complexSubpaths"
                    :key="index"
                    :property="subpath"
                    :is-last="index === complexSubpaths.length - 1"
                    @complex:click="reEmitComplexClick"
                    @simple:click="reEmitSimpleClick"
                    @add:click="reEmitAddClick"
                />
                <span
                    class="button-icon"
                    @click="$emit('add:click', property)"
                    @mouseenter="highlighted = true;"
                    @mouseleave="highlighted = false"
                >
                    <IconPlusSquare />
                </span>
                <!--
                <span
                    v-if="property.subpaths.length === 0"
                    class="fillerRow"
                >
                    &nbsp;
                </span>
                -->
            </div>
        </div>
        <div class="row">
            <span
                ref="bracketText"
                class="bracket-text"
                :class="{ highlighted }"
                @mouseenter="highlighted = true"
                @mouseleave="highlighted = false"
            >
                }{{ isLast ? '' : ',' }}
            </span>
        </div>
    </div>
</template>

<style scoped>
.outer {
    display: flex;
    flex-direction: column;
}

.divider {
    display: flex;
    flex-direction: row;
}

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

.inner {
    display: flex;
    flex-direction: column;
}

.highlighted {
    background-color: var(--color-background-dark);
}

.name-text, .bracket-text {
    cursor: pointer;
    width: fit-content;
    padding: 2px 4px;
    border-radius: 4px;
}

.fillerRow {

}
</style>
