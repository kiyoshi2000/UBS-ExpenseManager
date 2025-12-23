/// <reference types="vitest" />
import path from "path"
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './test/setup.ts',
    include: ['src/**/*.{test,spec}.{ts,tsx}', 'test/**/*.{test,spec}.{ts,tsx}'],
    exclude: ['test/e2e/**'],
  },
})
