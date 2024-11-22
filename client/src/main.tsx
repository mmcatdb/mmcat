import React from 'react';
import ReactDOM from 'react-dom/client';
import { NextUIProvider } from '@nextui-org/react';
import '@/assets/index.css';
import { PreferencesProvider } from './components/PreferencesProvider';
import 'react-toastify/dist/ReactToastify.css';
import App from '@/App';

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <NextUIProvider>
            <PreferencesProvider>
                <App />
            </PreferencesProvider>
        </NextUIProvider>
    </React.StrictMode>,
);
