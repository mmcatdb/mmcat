<template>
    <nav
        id="app-left-bar-content"
        class="left-bar flex-column"
    >
        <slot />
    </nav>
</template>

<style>
.left-bar {
    position: fixed;
    top: var(--top-bar-height);
    bottom: 0px;
    width: var(--left-bar-width);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    padding: 10px;
    font-size: 20px;
}

.left-bar a {
  display: inline-flex;
  padding: 0 16px;
  border-top: 1px solid var(--color-border);
}

.left-bar a:first-of-type {
  border: 0;
}
</style>
