import { StrictMode } from 'react';
import ReactDOM from 'react-dom/client';
import { HeroUIProvider } from '@heroui/react';
import '@/assets/index.css';
import { PreferencesProvider } from './components/context/PreferencesProvider';
import 'react-toastify/dist/ReactToastify.css';
import { App } from '@/App';

ReactDOM.createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <HeroUIProvider>
            <PreferencesProvider>
                <App />
            </PreferencesProvider>
        </HeroUIProvider>
    </StrictMode>,
);
