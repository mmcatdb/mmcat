# example-ui

A frontend application for the MM-evocat tool. It is based on [Vue 3](https://vuejs.org/) ([Vite](https://vitejs.dev/)) + TypeScript + Options API.

## Requirements

- node 16.13
- npm 8.1

## Configuration

- All the configuration should be done via the `.env` files.
- For start, simply create a `.env.local` file from the `.env.local.sample` and fill all required information.
```sh
cp .env.local.sample .env.local
```
- How to configure different modes is described in the [Modes and Environment Variables](https://vitejs.dev/guide/env-and-mode.html).
- For more advanced options see the [Vite Configuration Reference](https://vitejs.dev/config/).

## Installation

### Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```
