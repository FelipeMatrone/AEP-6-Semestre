/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base do backend Spring, ex: http://localhost:8080 */
  readonly VITE_API_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
