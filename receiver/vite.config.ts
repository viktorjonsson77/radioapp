import { defineConfig } from "vite";

export default defineConfig({
  // GitHub Pages project site: https://viktorjonsson77.github.io/radioapp/
  base: "/radioapp/",
  build: {
    outDir: "dist",
    emptyOutDir: true,
    sourcemap: true,
    target: "es2020",
  },
  test: {
    environment: "node",
    coverage: { reporter: ["text", "html"] },
  },
});
