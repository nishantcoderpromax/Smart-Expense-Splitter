import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['apple-touch-icon.png'],
      manifest: {
        name: 'SplitLedger',
        short_name: 'SplitLedger',
        description: 'Every shared expense, settled fairly.',
        theme_color: '#B8892B',   // brass accent
        background_color: '#F7F3EB', // paper background
        display: 'standalone',
        start_url: '/dashboard',
        icons: [
          { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' },
          { src: 'maskable-icon-512x512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // Cache the app shell + static assets. API calls (/groups, /expenses, etc.)
        // are deliberately NOT cached here — this app's data changes too often
        // (live balances, WebSocket updates) for a stale-while-revalidate cache
        // to be safe. Offline support covers "the app loads," not "data is fresh."
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        navigateFallbackDenylist: [/^\/api/, /^\/ws/],
      },
      devOptions: {
        enabled: false, // avoid service-worker caching interfering with `npm run dev` HMR
      },
    }),
  ],
  // sockjs-client (used for WebSocket fallback) assumes Node's `global` object
  // exists, which browsers/Vite don't provide — this maps it to globalThis.
  define: {
    global: 'globalThis',
  },
})

