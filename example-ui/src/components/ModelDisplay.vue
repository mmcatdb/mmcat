<script lang="ts">
import type { Model } from '@/types/model';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        model: {
            type: Object as () => Model,
            required: true
        }
    },
    data() {
        return {

        };
    },
    mounted() {
        this.resizeTextArea();
    },
    methods: {
        resizeTextArea() {
            const textArea = this.$refs.textArea as HTMLTextAreaElement;
            textArea.style.height = textArea.scrollHeight + 'px';
        }
    }
});
</script>

<template>
    <div class="model-display">
        <h2>{{ model.jobLabel }}</h2>
        <table>
            <tr>
                <td class="label">
                    Job id:
                </td>
                <td class="value">
                    {{ model.jobId }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Job label:
                </td>
                <td class="value">
                    {{ model.jobLabel }}
                </td>
            </tr>
        </table>
        <textarea
            ref="textArea"
            class="model-commands"
            spellcheck="false"
            :value="model.commands"
            :disabled="true"
            @input="resizeTextArea"
        />
    </div>
</template>

<style scoped>
.model-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 244px;
}

.model-commands {
    color: white;
    background-color: var(--color-background-dark);
    font-family: Consolas, monospace;
    min-width: 600px;
    font-size: 15px;
    overflow: hidden;
    resize: none;
    padding: 8px;
    border-radius: 8px;
}
</style>
