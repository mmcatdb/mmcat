<script lang="ts">
import { GraphSimpleProperty, GraphComplexProperty, type GraphParentProperty, GraphRootProperty } from '@/types/accessPath/graph';
import { SimpleProperty, ComplexProperty, type ParentProperty } from '@/types/accessPath/basic';
import { defineComponent } from 'vue';
import SimplePropertyDisplay from './SimplePropertyDisplay.vue';
import ButtonIcon from '@/components/ButtonIcon.vue';
import IconPlusSquare from '@/components/icons/IconPlusSquare.vue';

export default defineComponent({
    name: 'ParentPropertyDisplay',
    components: {
        SimplePropertyDisplay,
        ButtonIcon,
        IconPlusSquare
    },
    props: {
        property: {
            type: Object as () => GraphParentProperty | ParentProperty,
            required: true
        },
        isLast: {
            type: Boolean,
            default: true,
            required: false
        },
        isRoot: {
            type: Boolean,
            default: true,
            required: false
        },
        disableAdditions: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'complex:click', 'simple:click', 'add:click' ],
    data() {
        return {
            highlighted: false
        };
    },
    computed: {
        simpleSubpaths(): (GraphSimpleProperty | SimpleProperty)[] {
            return this.property instanceof GraphRootProperty || this.property instanceof GraphComplexProperty ?
                this.property.subpaths.filter((subpath): subpath is GraphSimpleProperty => subpath instanceof GraphSimpleProperty) :
                this.property.subpaths.filter((subpath): subpath is SimpleProperty => subpath instanceof SimpleProperty);
        },
        complexSubpaths(): (GraphComplexProperty | ComplexProperty)[] {
            return this.property instanceof GraphRootProperty || this.property instanceof GraphComplexProperty ?
                this.property.subpaths.filter((subpath): subpath is GraphComplexProperty => subpath instanceof GraphComplexProperty) :
                this.property.subpaths.filter((subpath): subpath is ComplexProperty => subpath instanceof ComplexProperty);
        }
    },
    methods: {
        reEmitComplexClick(property: GraphComplexProperty): void {
            this.$emit('complex:click', property);
        },
        reEmitSimpleClick(property: GraphSimpleProperty): void {
            this.$emit('simple:click', property);
        },
        reEmitAddClick(property: GraphComplexProperty): void {
            this.$emit('add:click', property);
        },
        emitComplexClick(): void {
            if (!this.isRoot)
                this.$emit('complex:click', this.property);
        }
    }
});
</script>


<template>
    <div class="outer">
        <div class="row">
            <span
                class="name-text"
                :class="{ highlighted, clickable: !isRoot }"
                @click="emitComplexClick"
                @mouseenter="highlighted = true;"
                @mouseleave="highlighted = false"
            >
                {{ property.name }}: {{ property.isAuxiliary ? '' : (property.signature + ' ') }}{
            </span>
        </div>
        <div class="property-divide">
            <div class="filler">
                <div
                    class="filler-line"
                    :class="{ highlighted }"
                />
            </div>
            <div class="inner">
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
                <ButtonIcon
                    v-if="!disableAdditions"
                    class="name-text"
                    @click="$emit('add:click', property)"
                    @mouseenter="highlighted = true;"
                    @mouseleave="highlighted = false"
                >
                    <IconPlusSquare />
                </ButtonIcon>
            </div>
        </div>
        <div class="row">
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
.outer {
    display: flex;
    flex-direction: column;
}

.property-divide {
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
    width: fit-content;
    padding: 2px 4px;
    border-radius: 4px;
}
</style>
