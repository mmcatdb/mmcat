<template>
    <nav class="leftBar">
        <slot />
    </nav>
</template>

<style>
.leftBar {
    position: fixed;
    top: 64px;
    bottom: 0px;
    width: var(--left-panel-width);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    padding: 10px;
    font-size: 20px;
}

a {
  text-decoration: none;
  color: var(--color-primary);
  transition: 0.4s;
}

@media (hover: hover) {
  a:hover {
    background-color: var(--color-primary-dark);
  }
}

a.router-link-exact-active {
  color: var(--color-text);
}

a.router-link-exact-active:hover {
  background-color: transparent;
}

a {
  display: inline-block;
  padding: 0 1rem;
  border-top: 1px solid var(--color-border);
}

a:first-of-type {
  border: 0;
}
</style>
