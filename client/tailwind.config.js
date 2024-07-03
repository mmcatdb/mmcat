import { nextui } from '@nextui-org/react';

/** @type {import('tailwindcss').Config} */
export default {
    content: [
        './index.html',
        './src/**/*.{js,ts,jsx,tsx}',
        './node_modules/@nextui-org/theme/dist/**/*.{js,ts,jsx,tsx}',
    ],
    theme: {
        extend: {},
        menuDimension: '4rem',
        contextWidth: '20rem',
        layoutBorderWidth: '1px',
    },
    darkMode: 'class',
    plugins: [ nextui() ],
}
