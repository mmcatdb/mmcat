import { useRouteError } from 'react-router-dom';
import { Button, Spinner } from '@nextui-org/react';

export function ErrorPage() {
    const error = useRouteError();
    console.error(error);

    return (
        <div>
            <h1 className='heading-main'>404</h1>
            <p>
                The page you are looking for does not exist.
            </p>
        </div>
    );
}

export function LoadingPage() {
    return (
        <div className='flex items-center justify-center pt-10'>
            <Spinner />
        </div>
    );
}

type ReloadPageProps = {
    onReload: () => void;
    title?: string;
    message?: string;
};

export function ReloadPage({ onReload, title = 'Error', message = 'Failed to load resource.' }: ReloadPageProps) {
    return (
        <div className='p-6'>
            <h1 className='text-xl font-semibold'>{title}</h1>
            <p className='text-pink-600 my-5'>{message}</p>
            <Button onPress={onReload} color='primary'>
                Reload
            </Button>
        </div>
    );
}
