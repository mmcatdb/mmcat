# mmcat-client-old

A frontend application for the MM-cat tool. It is based on [React](https://react.dev/?uwu=true) ([Vite](https://vitejs.dev/)) + TypeScript + Options API.

## Requirements

- node 22.3
- npm 10.8

## Configuration

- All the configuration should be done via the `.env` files.
- For start, simply create a `.env` file from the `.env.sample` and fill all the required information.
```sh
cp .env.sample .env
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

### Type-Check

```sh
npm run types
```

### Compile and Minify for Production

```sh
npm run build
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```
