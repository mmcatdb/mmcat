import { defineConfig, globalIgnores } from 'eslint/config';
import { fileURLToPath } from 'node:url';
import { includeIgnoreFile, fixupPluginRules } from '@eslint/compat';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import js from '@eslint/js';
import stylistic from '@stylistic/eslint-plugin';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';

// A lightweight config with mostly inexpensive rules and without type-aware lintig. Ideal for everyday usage in IDE.

export default defineConfig([
    ...[
        '',
    ].map(path => includeIgnoreFile(fileURLToPath(new URL(`${path}.gitignore`, import.meta.url)))),

    globalIgnores([
        'eslint.config.ts',
        'eslint.full.config.ts',
    ]),

    {
        files: [ '**/*.jsx', '**/*.ts', '**/*.tsx', '**/*.cts', '**.*.mts' ],

        languageOptions: {
            globals: {
                ...globals.browser,
            },
            parser: tseslint.parser,
            ecmaVersion: 'latest',
            sourceType: 'module',
            // Type-aware linting is explicitely turned off - use the full linting config for the final lint.
            parserOptions: {}
        },

        settings: {
            react: {
                version: 'detect',
            },
        },

        plugins: {
            js,
            '@stylistic': stylistic,
            '@typescript-eslint': tseslint.plugin,
            react,
            // TODO Fix remove the compat function once reactHooks implements the new eslint api.
            'react-hooks': fixupPluginRules(reactHooks),
            'react-refresh': reactRefresh,
        },

        extends: [
            js.configs.recommended,
            tseslint.configs.recommended,
            react.configs.flat.recommended,
            react.configs.flat['jsx-runtime'],
        ],

        rules: {
            'no-eval': [ 'error' ],
            'no-warning-comments': [ 'warn', {
                terms: [ 'TODO', 'FIXME', 'NICE_TO_HAVE' ],
            } ],
            'curly': [ 'warn', 'multi-or-nest', 'consistent' ],

            // Replaced by the @typescript-eslint rules.
            'no-unused-vars': 'off',
            'no-empty-function': 'off',

            '@stylistic/semi': [ 'error', 'always' ],
            '@stylistic/indent': [ 'warn', 4, {
                ignoredNodes: [
                    // A workaround for decorators. Not ideal, though.
                    'FunctionExpression > .params[decorators.length > 0]',
                    'FunctionExpression > .params > :matches(Decorator, :not(:first-child))',
                    'ClassBody.body > PropertyDefinition[decorators.length > 0] > .key',
                ],
            } ],
            '@stylistic/array-bracket-spacing': [ 'warn', 'always' ],
            '@stylistic/object-curly-spacing': [ 'warn', 'always' ],
            '@stylistic/space-before-function-paren': [ 'warn', {
                anonymous: 'always',
                named: 'never',
                asyncArrow: 'always',
            } ],
            '@stylistic/brace-style': [ 'warn', 'stroustrup' ],
            '@stylistic/nonblock-statement-body-position': [ 'error', 'below' ],
            '@stylistic/comma-dangle': [ 'warn', 'always-multiline' ],
            '@stylistic/quotes': [ 'warn', 'single', {
                allowTemplateLiterals: 'always',
            } ],
            '@stylistic/jsx-quotes': [ 'warn', 'prefer-single' ],
            '@stylistic/arrow-parens': [ 'warn', 'as-needed' ],
            '@stylistic/member-delimiter-style': [ 'error', {
                singleline: {
                    delimiter: 'comma',
                },
            } ],

            // TypeScript
            '@typescript-eslint/no-unused-vars': [ 'error', {} ],
            '@typescript-eslint/no-empty-function': [ 'warn', {
                allow: [ 'private-constructors' ],
            } ],
            '@typescript-eslint/consistent-type-imports': [ 'error', {
                fixStyle: 'inline-type-imports',
            } ],
            '@typescript-eslint/no-explicit-any': [ 'warn' ],

            // React
            'react/display-name': [ 'off' ],
            'react/jsx-no-target-blank': [ 'error', {
                allowReferrer: true,
            } ],
            // These should be already covered by typescript.
            'react/prop-types': [ 'off' ],
            'react/no-unknown-property': [ 'off' ],
            // These are enabled in full config.
            'react/no-direct-mutation-state': [ 'off' ],
            'react/require-render-return': [ 'off' ],
            'react/no-render-return-value': [ 'off' ],

            'react-refresh/only-export-components': [ 'warn', {
                allowConstantExport: true,
            } ],

            ...reactHooks.configs.recommended.rules,
            'react-hooks/exhaustive-deps': [ 'warn' ],
        },
    },
]);
