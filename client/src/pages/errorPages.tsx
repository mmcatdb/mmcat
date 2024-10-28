import { useRouteError } from 'react-router-dom';
import { usePreferences } from '@/components/PreferencesProvider';
import { Spinner } from '@nextui-org/react';
import clsx from 'clsx';

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
        <div>
            <Spinner />
        </div>
    );
}

type LoadingComponentProps = Readonly<{
    className?: string;
}>;

export function LoadingComponent({ className }: LoadingComponentProps) {
    const { theme } = usePreferences().preferences;

    return (
        <div className={clsx('flex items-center justify-center w-full h-full', theme, className)}>
            <div className='animate-spin w-16 h-16 border-t-2 border-b-2 border-primary rounded-full' />
        </div>
    );
}
