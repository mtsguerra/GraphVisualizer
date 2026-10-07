import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // The backend's CORS config allows http://localhost:3000.
    port: 3000,
    strictPort: true,
  },
})
