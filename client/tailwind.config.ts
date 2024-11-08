import type { Config } from 'tailwindcss';
import { nextui } from '@nextui-org/react';

export default {
    darkMode: 'class',
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
    plugins: [ nextui() ],
} satisfies Config;
