import { isRouteErrorResponse, useRouteError } from 'react-router-dom';
import { Button, Spinner } from '@nextui-org/react';
import { cn } from '@/components/utils';

type RouteError = {
  status?: number;
  statusText?: string;
  message?: string;
  stack?: string;
}

/**
 * Error page component that handles 404 and other exceptions.
 * In development: Shows debug logs, stack trace, and error message on the page.
 * In production: Shows only the error message, no debug logs or stack trace.
 */
export function ErrorPage() {
    const error = useRouteError();
    const isDev = import.meta.env.DEV;

    // Debug logs for development mode only
    const debugLogs = isDev
        ? [
            `ErrorPage error: ${JSON.stringify(error, null, 2)}`,
            `Error object: ${JSON.stringify(error, null, 2)}`,
        ]
        : [];

    const isNotFound = isRouteErrorResponse(error) && error.status === 404;

    const errorStyles = isNotFound
        ? 'bg-yellow-100 text-yellow-800 border-yellow-300'
        : 'bg-red-100 text-red-800 border-red-300';

    // Extract error message
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const errorMessage = isRouteErrorResponse(error)
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
        ? error.statusText || error.data?.message || 'The page you are looking for does not exist.'
        : (error as RouteError)?.message ?? (error instanceof Error ? error.message : 'An unexpected error occurred.');

    const errorStack = isDev ? (error as RouteError)?.stack ?? (error instanceof Error ? error.stack : undefined) : undefined;

    return (
        <div className={cn('min-h-screen flex flex-col items-center justify-center p-4', errorStyles)}>
            <h1 className='text-4xl font-bold mb-4'>
                {isNotFound ? '404 - Page Not Found' : 'Something Went Wrong'}
            </h1>
            <p className='text-lg mb-6 text-center'>{errorMessage}</p>
            {isDev && errorStack && (
                <div className='w-full max-w-2xl bg-gray-800 text-white p-4 rounded-md overflow-auto max-h-96'>
                    <h2 className='text-xl font-semibold mb-2'>Stack Trace (Development Only)</h2>
                    <pre className='text-sm whitespace-pre-wrap'>{errorStack}</pre>
                </div>
            )}
            {isDev && debugLogs.length > 0 && (
                <div className='w-full max-w-2xl bg-gray-200 text-gray-800 p-4 rounded-md mt-4'>
                    <h2 className='text-xl font-semibold mb-2'>Debug Logs (Development Only)</h2>
                    <pre className='text-sm whitespace-pre-wrap'>{debugLogs.join('\n\n')}</pre>
                </div>
            )}
            <Button
                color={isNotFound ? 'warning' : 'danger'}
                onPress={() => window.location.assign('/')}
                className='mt-4'
            >
                Go to Home
            </Button>
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
