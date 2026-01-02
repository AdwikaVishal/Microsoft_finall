import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  root: 'src/apps/admin-dashboard',
  build: {
    outDir: '../../../dist/admin-dashboard'
  },
  server: {
    port: 3001
  }
})
