import type { Config } from 'tailwindcss';
import { nextui } from '@nextui-org/react';

export default {
    darkMode: 'class',
    content: [
        './index.html',
        './src/**/*.{js,ts,jsx,tsx}',
        './node_modules/@nextui-org/theme/dist/**/*.{js,ts,jsx,tsx}',
    ],
    important: false,
    theme: {
        fontSize: {
            xs: '0.625rem',     // 10px
            sm: '0.75rem',      // 12px
            base: '0.875rem',   // 14px
            lg: '1rem',         // 16px
            xl: '1.25rem',      // 20px
            '2xl': '1.5rem',    // 24px
            '3xl': '1.75rem',   // 28px
            '4xl': '2rem',      // 32px
            '5xl': '2.25rem',   // 36px
            '6xl': '2.5rem',    // 40px
        },
        extend: {
            colors: {
                canvas: {
                    light: '#e4e4e7',
                    dark: '#000000',
                },
            },
        },
        menuDimension: '4rem',
        contextWidth: '20rem',
        layoutBorderWidth: '1px',
    },
    plugins: [ nextui() ],
} satisfies Config;
