<template>
    <header class="top-bar">
        <div
            id="app-top-bar-left"
            class="top-bar-content top-bar-left"
        >
            <slot name="left" />
        </div>
        <div
            id="app-top-bar-center"
            class="top-bar-content top-bar-center"
        >
            <slot name="center" />
        </div>
    </header>
</template>

<style scoped>
.top-bar {
    position: fixed;
    top: 0;
    width: 100%;
    height: var(--top-bar-height);
    background-color: var(--color-primary);
}

.top-bar-content {
    position: fixed;
    top: 0;
    height: var(--top-bar-height);
    overflow: hidden;
    display: flex;
    align-items: center;
}

.top-bar-left {
    padding: 10px;
}

.top-bar-center {
    left: var(--left-bar-width);
    padding: 10px 20px;
}
</style>
