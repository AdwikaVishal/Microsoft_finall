import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  root: 'src/apps/admin-dashboard',
  build: {
    outDir: '../../../dist/admin-dashboard'
  },
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://192.168.0.130:8000',
        changeOrigin: true,
        secure: false,
      },
      '/auth': {
        target: 'http://192.168.0.130:8000',
        changeOrigin: true,
        secure: false,
      },
      '/health': {
        target: 'http://192.168.0.130:8000',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})

