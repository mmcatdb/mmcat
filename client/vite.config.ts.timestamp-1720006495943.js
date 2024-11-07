// vite.config.ts
import { defineConfig, loadEnv } from "vite";
import { fileURLToPath, URL } from "url";
import react from "@vitejs/plugin-react-swc";
import { createHtmlPlugin } from "vite-plugin-html";
var vite_config_default = defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const base = env.ENV_BASE_URL ?? "/";
  return {
    plugins: [
      react(),
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
        "@": fileURLToPath(new URL("./src", "file:///C:/Users/alzbe/Documents/mff_mgr/Diplomka/Apps/mmcat/client/vite.config.ts"))
      }
    },
    base,
    server: {
      port: Number.parseInt(env.VITE_DEV_SERVER_PORT)
    }
  };
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImltcG9ydCB7IGRlZmluZUNvbmZpZywgbG9hZEVudiB9IGZyb20gJ3ZpdGUnO1xyXG5pbXBvcnQgeyBmaWxlVVJMVG9QYXRoLCBVUkwgfSBmcm9tICd1cmwnO1xyXG5pbXBvcnQgcmVhY3QgZnJvbSAnQHZpdGVqcy9wbHVnaW4tcmVhY3Qtc3djJztcclxuaW1wb3J0IHsgY3JlYXRlSHRtbFBsdWdpbiB9IGZyb20gJ3ZpdGUtcGx1Z2luLWh0bWwnO1xyXG5cclxuLy8gaHR0cHM6Ly92aXRlanMuZGV2L2NvbmZpZy9cclxuZXhwb3J0IGRlZmF1bHQgZGVmaW5lQ29uZmlnKCh7IG1vZGUgfSkgPT4ge1xyXG4gICAgY29uc3QgZW52ID0gbG9hZEVudihtb2RlLCBwcm9jZXNzLmN3ZCgpLCAnJyk7XHJcbiAgICBjb25zdCBiYXNlID0gZW52LkVOVl9CQVNFX1VSTCA/PyAnLyc7XHJcblxyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICBwbHVnaW5zOiBbXHJcbiAgICAgICAgICAgIHJlYWN0KCksXHJcbiAgICAgICAgICAgIGNyZWF0ZUh0bWxQbHVnaW4oe1xyXG4gICAgICAgICAgICAgICAgaW5qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZGF0YToge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBhcHBfdmVyc2lvbjogcHJvY2Vzcy5lbnYubnBtX3BhY2thZ2VfdmVyc2lvbixcclxuICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgXSxcclxuICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgIGFsaWFzOiB7XHJcbiAgICAgICAgICAgICAgICAnQCc6IGZpbGVVUkxUb1BhdGgobmV3IFVSTCgnLi9zcmMnLCBcImZpbGU6Ly8vQzovVXNlcnMvYWx6YmUvRG9jdW1lbnRzL21mZl9tZ3IvRGlwbG9ta2EvQXBwcy9tbWNhdC9jbGllbnQvdml0ZS5jb25maWcudHNcIikpLFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgIH0sXHJcbiAgICAgICAgYmFzZSxcclxuICAgICAgICBzZXJ2ZXI6IHtcclxuICAgICAgICAgICAgcG9ydDogTnVtYmVyLnBhcnNlSW50KGVudi5WSVRFX0RFVl9TRVJWRVJfUE9SVCksXHJcbiAgICAgICAgfSxcclxuICAgIH07XHJcbn0pO1xyXG4iXSwKICAibWFwcGluZ3MiOiAiO0FBQUEsU0FBUyxjQUFjLGVBQWU7QUFDdEMsU0FBUyxlQUFlLFdBQVc7QUFDbkMsT0FBTyxXQUFXO0FBQ2xCLFNBQVMsd0JBQXdCO0FBR2pDLElBQU8sc0JBQVEsYUFBYSxDQUFDLEVBQUUsS0FBSyxNQUFNO0FBQ3RDLFFBQU0sTUFBTSxRQUFRLE1BQU0sUUFBUSxJQUFJLEdBQUcsRUFBRTtBQUMzQyxRQUFNLE9BQU8sSUFBSSxnQkFBZ0I7QUFFakMsU0FBTztBQUFBLElBQ0gsU0FBUztBQUFBLE1BQ0wsTUFBTTtBQUFBLE1BQ04saUJBQWlCO0FBQUEsUUFDYixRQUFRO0FBQUEsVUFDSixNQUFNO0FBQUEsWUFDRixhQUFhLFFBQVEsSUFBSTtBQUFBLFVBQzdCO0FBQUEsUUFDSjtBQUFBLE1BQ0osQ0FBQztBQUFBLElBQ0w7QUFBQSxJQUNBLFNBQVM7QUFBQSxNQUNMLE9BQU87QUFBQSxRQUNILEtBQUssY0FBYyxJQUFJLElBQUksU0FBUyxvRkFBb0YsQ0FBQztBQUFBLE1BQzdIO0FBQUEsSUFDSjtBQUFBLElBQ0E7QUFBQSxJQUNBLFFBQVE7QUFBQSxNQUNKLE1BQU0sT0FBTyxTQUFTLElBQUksb0JBQW9CO0FBQUEsSUFDbEQ7QUFBQSxFQUNKO0FBQ0osQ0FBQzsiLAogICJuYW1lcyI6IFtdCn0K
