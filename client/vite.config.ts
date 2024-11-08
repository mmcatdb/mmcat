import { defineConfig, loadEnv } from 'vite';
import { fileURLToPath, URL } from 'url';
import react from '@vitejs/plugin-react-swc';
import { createHtmlPlugin } from 'vite-plugin-html';
import tailwindcss from 'tailwindcss';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '');
    const base = env.ENV_BASE_URL ?? '/';

    return {
        plugins: [
            react(),
            createHtmlPlugin({
                inject: {
                    data: {
                        app_version: process.env.npm_package_version,
                    },
                },
            }),
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
        css: {
            postcss: {
                plugins: [ tailwindcss ],
            },
        },
    };
});
