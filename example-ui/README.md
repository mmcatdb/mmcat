# example-ui

A frontend application for the MM-cat tool. It is based on Vue3 ([Vite](https://vitejs.dev/)) + typescript.

## Requirements

- node 16.13
- npm 8.1

## Configuration

- All the configuration should be done via the `.env` files.
- For start, simply create a `.env.local` file from the `.env.local.sample` and fill all required information.
```sh
cp .env.local .env.local.sample
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
