import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  root: 'src/frontend',
  publicDir: false,
  build: {
    outDir: resolve(__dirname, 'webapp/assets'),
    emptyOutDir: true,
    assetsDir: '',
    manifest: true,
    rollupOptions: {
      input: resolve(__dirname, 'src/frontend/main.js'),
      output: {
        entryFileNames: 'js/app-bundle.js',
        chunkFileNames: 'js/[name]-chunk.js',
        assetFileNames: ({ name }) => {
          if (name && name.endsWith('.css')) {
            return 'css/[name]';
          }
          return 'assets/[name][extname]';
        },
      },
    },
  },
});
