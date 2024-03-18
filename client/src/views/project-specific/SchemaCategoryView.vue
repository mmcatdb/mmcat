<script setup lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { provide, shallowRef } from 'vue';
import EditorForSchemaCategory from '@/components/category/edit/EditorForSchemaCategory.vue';
import { evocatKey, type EvocatContext } from '@/utils/injects';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from '@/components/category/EvocatDisplay.vue';
import VersionsControl from '@/components/category/version/VersionsControl.vue';

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
provide(evocatKey, { evocat, graph } as EvocatContext);

function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    graph.value = context.graph;
}

</script>

<template>
    <div class="divide">
        <EvocatDisplay
            @evocat-created="evocatCreated"
        />
        <div
            v-if="evocat"
        >
            <EditorForSchemaCategory />
        </div>
    </div>
    <div v-if="evocat">
        <VersionsControl />
    </div>
</template>
