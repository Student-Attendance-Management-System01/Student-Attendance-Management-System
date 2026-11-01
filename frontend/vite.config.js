import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// The dev server proxies /api to the Spring Boot backend so the browser sees a
// single origin (needed for the HttpOnly auth cookie and the XSRF cookie).
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': { target: env.SAMS_API_PROXY || 'http://localhost:8080', changeOrigin: true }
      }
    }
  };
});
