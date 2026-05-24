import { defineConfig } from 'vite';
import { resolve } from 'path';

function fallbackBundlePlugin() {
  return {
    name: 'fallback-bundle-emitter',
    generateBundle(_options, bundle) {
      const entryChunk = Object.values(bundle).find(
        (item) => item.type === 'chunk' && item.isEntry,
      );

      if (entryChunk && entryChunk.type === 'chunk') {
        this.emitFile({
          type: 'asset',
          fileName: 'js/app-bundle.js',
          source: entryChunk.code,
        });
      }
    },
  };
}

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
        entryFileNames: 'js/[name]-[hash].js',
        chunkFileNames: 'js/[name]-[hash].js',
        assetFileNames: ({ name }) => {
          if (name && name.endsWith('.css')) {
            return 'css/[name]-[hash][extname]';
          }
          return 'assets/[name]-[hash][extname]';
        },
      },
    },
  },
  plugins: [fallbackBundlePlugin()],
});
