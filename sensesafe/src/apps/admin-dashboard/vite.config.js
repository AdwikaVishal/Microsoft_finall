import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  root: '.',
  css: {
    postcss: './postcss.config.js'
  },
  build: {
    outDir: '../../../dist/admin-dashboard'
  },
  server: {
    port: 3001
  }
})
