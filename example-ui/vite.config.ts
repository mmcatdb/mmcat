import { fileURLToPath, URL } from 'url';

import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { createHtmlPlugin } from 'vite-plugin-html';

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
    // Explicitly load the env files because otherwise they would be loaded after the settings (because their location may depend on this settings).
    const env = loadEnv(mode, process.cwd(), '');
    const base = env.BASE_PATH || '/';

    return {
        plugins: [
            vue(),
            createHtmlPlugin({
                inject: {
                    data: {
                        app_version: process.env.npm_package_version
                    }
                }
            })
        ],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url))
            }
        },
        base,
        server: {
            cors: {
                origin: env.VITE_DATASPECER_API_URL
            }
        }
    };
});
