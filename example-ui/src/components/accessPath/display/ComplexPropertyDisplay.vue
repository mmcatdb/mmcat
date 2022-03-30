<script lang="ts">
import { SimpleProperty, ComplexProperty } from '@/types/accessPath';
import { defineComponent } from 'vue';
import SimplePropertyDisplay from './SimplePropertyDisplay.vue';

export default defineComponent({
    components: {
        SimplePropertyDisplay
    },
    props: {
        property: {
            type: Object as () => ComplexProperty,
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
            SimpleProperty: SimpleProperty
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
        <span @click="$emit('complex:click', property)">{{ property.name }}: {{ property.isAuxiliary ? '' : (property.signature + ' ') }}{</span>
        <div class="divider">
            <div class="filler" />
            <div class="inner">
                <span @click="$emit('add:click', property)">+</span>
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
                    v-if="property.subpaths.length === 0"
                    class="fillerRow"
                >
                    &nbsp;
                </span>
            </div>
        </div>
        <span>}{{ isLast ? '' : ',' }}</span>
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
}

.inner {
    display: flex;
    flex-direction: column;
}

.fillerRow {

}
</style>
