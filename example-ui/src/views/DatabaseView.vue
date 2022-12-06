<script lang="ts">
import { defineComponent } from 'vue';
import API from '@/utils/api';
import { getNewDatabaseUpdate, type Database } from '@/types/database';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';
import DatabaseEditor from '@/components/database/DatabaseEditor.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        DatabaseDisplay,
        DatabaseEditor
    },
    props: {

    },
    data() {
        const rawId = this.$route.params.id as string;
        const isNew = rawId === 'new';
        const id = isNew ? null : parseInt(rawId);
        const isEditing = isNew || this.$route.params.state === 'editing';

        return {
            id,
            isNew,
            isEditing,
            shouldReturnToAllDatabasesAfterEditing: isEditing,
            database: null as Database | null,
            newDatabase: getNewDatabaseUpdate(),
            fetched: false,
        };
    },
    mounted() {
        if (!this.isNew)
            this.fetchData();
    },
    methods: {
        async fetchData() {
            if (!this.id)
                return;

            const result = await API.databases.getDatabase({ id: this.id });
            if (result.status)
                this.database = result.data;

            this.fetched = true;
        },
        save(newValue: Database) {
            if (this.shouldReturnToAllDatabasesAfterEditing) {
                this.$router.push({ name: 'databases' });
                return;
            }

            this.database = newValue;
            this.isEditing = false;
        },
        cancel() {
            if (this.shouldReturnToAllDatabasesAfterEditing) {
                this.$router.push({ name: 'databases' });
                return;
            }

            this.isEditing = false;
        },
        deleteFunction() {
            this.$router.push({ name: 'databases' });
        }
    }
});
</script>

<template>
    <div>
        <template v-if="isNew">
            <h1>Create new database</h1>
            <div class="database">
                <DatabaseEditor
                    @save="save"
                    @cancel="cancel"
                />
            </div>
        </template>
        <template v-else>
            <h1>Database</h1>
            <div
                v-if="database"
                class="database"
            >
                <DatabaseEditor
                    v-if="isEditing"
                    :database="database"
                    @save="save"
                    @cancel="cancel"
                    @delete="deleteFunction"
                />
                <DatabaseDisplay
                    v-else
                    :database="database"
                    @edit="isEditing = true"
                />
            </div>
            <ResourceNotFound v-else-if="fetched" />
            <ResourceLoading v-else />
        </template>
    </div>
</template>

<style scoped>
.database {
    display: flex;
}
</style>
