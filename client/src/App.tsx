import { RouterProvider } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import { usePreferences } from '@/components/PreferencesProvider';
import { router } from '@/routes/router';

const App = () => {
    const { preferences } = usePreferences();

    return (
        <>
            <RouterProvider router={router} />
            <ToastContainer
                position='bottom-right'
                theme={preferences.theme}
                autoClose={2000}
            />
        </>
    );
};

export default App;
