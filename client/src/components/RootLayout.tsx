import { Suspense, useMemo } from 'react';
import clsx from 'clsx';
import { Button, Navbar, NavbarContent, NavbarItem, Breadcrumbs as NextUIBreadcrumbs, BreadcrumbItem } from '@nextui-org/react';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { usePreferences, type Theme } from './PreferencesProvider';
import { type AwaitedRouteData, type AwaitedUIMatch, Tooltip, useAwaitedMatches } from './common';
import { Sidebar } from './sidebar/Sidebar';
import { Await, Link, Outlet } from 'react-router-dom';

export function RootLayout() {
    const { theme } = usePreferences().preferences;

    return (
        <div className={clsx('h-screen overflow-hidden text-foreground bg-background', theme)}>
            <div className='flex h-full'>
                <Sidebar />
                <div className='flex flex-col flex-grow overflow-hidden'>
                    <CommonNavbar />
                    <main className='flex-grow overflow-auto w-full max-w-screen-xl mx-auto p-6'>
                        <Outlet />
                    </main>
                </div>
            </div>
        </div>
    );
}

function CommonNavbar() {
    const { theme } = usePreferences().preferences;

    return (
        <Navbar
            className={clsx(
                'z-20 w-full mx-auto h-12 border-b',
                theme === 'dark' ? 'border-gray-700' : 'border-gray-300',
            )}
            isBlurred={false}
            maxWidth='full'
        >
            <NavbarContent justify='start'>
                <Breadcrumbs />
            </NavbarContent>
            <NavbarContent justify='end'>
                <NavbarItem>
                    <ThemeToggle className='min-w-8 w-8 h-8' />
                </NavbarItem>
            </NavbarContent>
        </Navbar>
    );
}

function Breadcrumbs() {
    const promise = useAwaitedMatches();

    return (
        <Suspense>
            <Await resolve={promise}>
                {(matches: AwaitedUIMatch[]) => (
                    <BreadcrumbsLoaded matches={matches} />
                )}
            </Await>
        </Suspense>
    );
}

type BreadcrumbData = {
    path: string;
    label: string;
};

type BreadcrumbMatch<TData> = AwaitedUIMatch<TData, { breadcrumb: string | ((data: AwaitedRouteData<TData>) => string) }>;

type BreadcrumbsLoadedProps = Readonly<{
    matches: AwaitedUIMatch[];
}>;

function BreadcrumbsLoaded({ matches }: BreadcrumbsLoadedProps) {
    const breadcrumbs: BreadcrumbData[] = useMemo(() => {
        return matches.
            filter((match): match is BreadcrumbMatch<unknown> => !!(match.handle && typeof match.handle === 'object' && 'breadcrumb' in match.handle))
            .map(match => ({
                path: match.pathname,
                label: typeof match.handle.breadcrumb === 'function' ? match.handle.breadcrumb(match.awaited) : match.handle.breadcrumb,
            }));
    }, [ matches ]);

    return (
        // custom styles <Breadcrumbs className='breadcrumb'>, Link className='breadcrumb-item-link'
        <NextUIBreadcrumbs separator='/'>
            {breadcrumbs.map((crumb, index) => (
                <BreadcrumbItem key={crumb.path} isCurrent={index === breadcrumbs.length - 1}>
                    <Link to={crumb.path}> {crumb.label} </Link>
                </BreadcrumbItem>
            ))}
        </NextUIBreadcrumbs>
    );
}

type ThemeToggleProps = Readonly<{
    className?: string;
}>;

export function ThemeToggle({ className }: ThemeToggleProps) {
    const { preferences, setPreferences } = usePreferences();
    const { theme } = preferences;
    const nextValue = toggleTheme(theme);
    const label = `Switch to ${nextValue} theme`;

    return (
        <Tooltip content={label}>
            <Button
                isIconOnly
                aria-label={label}
                onPress={() => setPreferences({ ...preferences, theme: nextValue })}
                variant='faded'
                className={className}
            >
                {theme === 'dark' ? (
                    <MdOutlineDarkMode size={22} />
                ) : (
                    <MdOutlineLightMode size={22} />
                )}
            </Button>
        </Tooltip>
    );
}

function toggleTheme(theme: Theme) {
    return theme === 'dark' ? 'light' : 'dark';
}
