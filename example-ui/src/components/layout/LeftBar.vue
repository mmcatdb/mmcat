<template>
    <nav class="left-bar">
        <slot />
    </nav>
</template>

<style>
.left-bar {
    position: fixed;
    top: var(--top-bar-height);
    bottom: 0px;
    width: var(--left-panel-width);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    padding: 10px;
    font-size: 20px;
}

.left-bar a {
  display: inline-block;
  padding: 0 1rem;
  border-top: 1px solid var(--color-border);
}

.left-bar a:first-of-type {
  border: 0;
}
</style>
