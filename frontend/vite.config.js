import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/v1/admin/orders': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/v1/orders': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/carts': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
