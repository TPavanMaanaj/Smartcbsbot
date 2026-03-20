import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  optimizeDeps: {
    exclude: ['lucide-react'],
  },
  build: {
    lib: {
      entry: "src/embed.tsx",
      name: "SmartBotWidget",
      fileName: () => "smartbot-widget.js",
      formats: ["iife"],
    },
    rollupOptions: {
      external: [],
    },
  },
});
