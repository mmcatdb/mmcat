import React from 'react';
import ReactDOM from 'react-dom/client';
import { NextUIProvider } from '@nextui-org/react';
import '@/assets/index.css';
import { RouterProvider } from 'react-router-dom';
import { router } from './pages/router.tsx';
import { PreferencesProvider } from './components/PreferencesProvider.tsx';

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <NextUIProvider>
            <PreferencesProvider>
                <RouterProvider router={router} />
            </PreferencesProvider>
        </NextUIProvider>
    </React.StrictMode>,
);
