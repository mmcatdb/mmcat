import baseConfig from './eslint.config';
import { defineConfig } from 'eslint/config';

// A full eslint config with some hardcore rules. It's going to be expensive so lint only when needed (e.g., not in IDE).
// All errors from this lint should be fixed before commiting!

export default defineConfig([
    ...baseConfig,

    {
        // This config will only apply to the following files.
        // All options should be deep-merged with the baseConfig.
        files: [ '**/*.jsx', '**/*.ts', '**/*.tsx', '**/*.cts', '**.*.mts' ],

        languageOptions: {
            parserOptions: {
                projectService: 'true',
            },
        },

        rules: {
            // TypeScript
            '@typescript-eslint/unbound-method': [ 'error', {
                ignoreStatic: true,
            } ],
            '@typescript-eslint/no-misused-promises': [ 'error', {
                checksVoidReturn: false,
            } ],

            // React
            'react/no-direct-mutation-state': [ 'error' ],
            'react/require-render-return': [ 'error' ],
            'react/no-render-return-value': [ 'error' ],
        },
    },
]);
