import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config' // vitest's defineConfig also accepts the "test" section

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
  },
})
