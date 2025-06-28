import { defineConfig, loadEnv } from 'vite';
import { fileURLToPath, URL } from 'url';
import react from '@vitejs/plugin-react-swc';
import { createHtmlPlugin } from 'vite-plugin-html';

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
                        head: defineHead(env),
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
    };
});

function defineHead(env: Record<string, string>): string {
    let head = '';

    if (env.NODE_ENV === 'development' && (!env.VITE_REACT_SCAN_OFF || env.VITE_REACT_SCAN_OFF === 'false')) {
        head += `
            <script src="https://unpkg.com/react-scan/dist/auto.global.js"></script>
        `;
    }

    return head;
}
