/* eslint-env node */
require("@rushstack/eslint-patch/modern-module-resolution");

module.exports = {
    root: true,
    env: {
        node: true
    },
    extends: [
        'plugin:vue/vue3-recommended',
        'plugin:vue/vue3-essential',
        '@vue/typescript/recommended'
    ],
    parserOptions: {
        ecmaVersion: 2020
    },
    rules: {
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'semi': [ 'error', 'always' ],
        'indent': [ 'error', 4 ],
        'array-bracket-spacing': [ 'warn', 'always' ],
        'space-before-function-paren': [ 'warn', {
            'anonymous': 'always',
            'named': 'never',
            'asyncArrow': 'always'
        } ],
        'curly': [ 2, 'multi', 'consistent' ],
        'brace-style': [ 'warn', 'stroustrup' ]
    }
};
