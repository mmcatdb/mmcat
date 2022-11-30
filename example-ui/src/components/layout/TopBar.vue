<template>
    <header class="top-bar">
        <slot />
    </header>
</template>

<style scoped>
.top-bar {
    position: fixed;
    top: 0;
    width: 100%;
    height: var(--top-bar-height);
    overflow: hidden;
    background-color: var(--color-primary);
}
</style>
