import { useRouteError } from 'react-router-dom';
import { CommonPage } from '@/components/CommonPage';
import { usePreferences } from '@/components/PreferencesProvider';
import clsx from 'clsx';

export function ErrorPage() {
    const error = useRouteError();
    console.error(error);

    return (
        <CommonPage>
            <h1>404</h1>
            <p>
                The page you are looking for does not exist.
            </p>
        </CommonPage>
    );
}

export function LoadingPage() {
    return (
        <LoadingComponent className='w-screen h-screen text-foreground bg-background' />
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
