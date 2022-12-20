<script setup lang="ts">
import { provide } from 'vue';
import { RouterView, useRoute } from 'vue-router';

const route = useRoute();

provide('schemaCategoryId', route.params.schemaCategoryId);
</script>

<template>
    <RouterView />
</template>
