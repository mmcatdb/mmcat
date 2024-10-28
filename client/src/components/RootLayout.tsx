import clsx from 'clsx';
import { Button, Navbar, NavbarContent, NavbarItem, Breadcrumbs, BreadcrumbItem } from '@nextui-org/react';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { usePreferences, type Theme } from './PreferencesProvider';
import { Tooltip } from './common';
import { Sidebar } from './sidebar/Sidebar';
import { Link, Outlet, useMatches } from 'react-router-dom';

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
                {/* <div id='breadcrumb-portal'></div> */}
                <Breadcrumb />
            </NavbarContent>
            <NavbarContent justify='end'>
                <NavbarItem>
                    <ThemeToggle className='min-w-8 w-8 h-8' />
                </NavbarItem>
            </NavbarContent>
        </Navbar>
    );
}

type NavbarItem = {
    label: string;
    route: string;
};

function Breadcrumb() {
    const matches = useMatches();

    const breadcrumbs = matches
        .filter((match) => match.handle?.breadcrumb)  // filter out routes without breadcrumb handle
        .map((match) => ({
            label: match.handle!.breadcrumb as string,
            path: match.pathname,
        }));

    return (
        // custom styles <Breadcrumbs className='breadcrumb'>, Link className='breadcrumb-item-link'
        <Breadcrumbs separator='/'>
            {breadcrumbs.map((crumb, index) => (
                <BreadcrumbItem key={crumb.path} isCurrent={index === breadcrumbs.length - 1}>
                    <Link to={crumb.path}> {crumb.label} </Link>
                </BreadcrumbItem>
            ))}
        </Breadcrumbs>
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
