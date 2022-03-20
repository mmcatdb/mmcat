<script lang="ts">
import type { Core } from 'cytoscape';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        /*
        cytoscape: {
            type: Object,
            default: null
        }
        */
        cytoscape: null
    },
    //emits: [ 'update:modelValue' ],
    data() {
        return {
            lastUpdate: 'nothing yet'
        };
    },
    mounted() {
        (this.cytoscape as Core).addListener('tap', 'node', (event) => {
            let node = event.target;
            console.log('Tap on node:', node, node.id());
            console.log(this.cytoscape);
            this.lastUpdate = node.id();
        });
    }
});
</script>

<template>
    <div>
        <h2>Access path graphic editor</h2>
        <br>
        {{ lastUpdate }}
    </div>
</template>

<style scoped>
.accessPathInput {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}
</style>
