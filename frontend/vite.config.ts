import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // In dev the API runs on Spring Boot's port. The browser only sees the Vite address, so the session cookie just works
    proxy: { '/api': 'http://localhost:8080' },
  },
})