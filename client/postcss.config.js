// This file needs to be named .js because the postcss doesn't support .ts files. Would you believe that?

export default {
    plugins: {
        'postcss-import': {},
        'tailwindcss/nesting': {},
        tailwindcss: {},
        autoprefixer: {},
    },
};
