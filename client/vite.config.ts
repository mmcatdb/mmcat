import { fileURLToPath, URL } from 'url';

import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { createHtmlPlugin } from 'vite-plugin-html';
import ReactivityTransform from '@vue-macros/reactivity-transform/vite';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    // Explicitly load the env files because otherwise they would be loaded after the settings (because their location may depend on this settings).
    const env = loadEnv(mode, process.cwd(), '');
    const base = env.BASE_PATH || '/';

    return {
        plugins: [
            vue(),
            createHtmlPlugin({
                inject: {
                    data: {
                        app_version: process.env.npm_package_version,
                    },
                },
            }),
            ReactivityTransform(),
        ],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
            },
        },
        base,
        server: {
            port: Number.parseInt(env.VITE_DEV_SERVER_PORT),
        },
    };
});
