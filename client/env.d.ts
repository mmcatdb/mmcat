/// <reference types="vite/client" />
/// <reference types="vue/macros-global" />

interface ImportMetaEnv {
    readonly VITE_BACKEND_API_URL: string;
    readonly VITE_DOCUMENTATION_URL: string;
    readonly VITE_DATASPECER_API_URL: string;
    readonly VITE_DATASPECER_EXAMPLE_IRI: string;
    readonly VITE_CUSTOM_IRI_PREFIX: string;
}
