<template>
    <header class="topBar">
        <slot />
    </header>
</template>

<style scoped>
.topBar {
    position: fixed;
    top: 0;
    width: 100%;
    height: 64px;
    overflow: hidden;
    background-color: var(--color-primary);
}
</style>
