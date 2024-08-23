import clsx from 'clsx';
import { Button, Navbar, NavbarContent, NavbarItem } from '@nextui-org/react';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { usePreferences, type Theme } from './PreferencesProvider';
import { Link, Tooltip } from './common';
import { routes } from '@/pages/routes';
import { useLocation } from 'react-router-dom';

type CommonPageProps = Readonly<{
    children: React.ReactNode;
}>;

export function CommonPage({ children }: CommonPageProps) {
    const { theme } = usePreferences().preferences;

    return (
        <div className={clsx('h-screen overflow-hidden text-foreground bg-background', theme)}>
            <div className='main-scroller'>
                <CommonNavbar />
                <main className='w-full max-w-[1024px] mx-auto p-6'>
                    {children}
                </main>
            </div>
        </div>
    );
}

function CommonNavbar() {
    const location = useLocation();

    return (
        <Navbar>
            <NavbarContent justify='center'>
                {navbarItems.map(item => {
                    const isActive = item.route === location.pathname;

                    return (
                        <NavbarItem key={item.route} isActive={isActive}>
                            <Link to={item.route} isDisabled={isActive} color={isActive ? undefined : 'foreground'} className='data-[disabled=true]:opacity-100'>
                                {item.label}
                            </Link>
                        </NavbarItem>
                    );
                })}
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

const navbarItems: NavbarItem[] = [
    {
        label: 'Home',
        route: routes.home.path,
    },
    {
        label: 'About',
        // TODO: figure out why it does not work via routes (adding strings to the path, not changing them)
        // route: routes.about
        route: '/about',
    },
    {
        label: 'Datasources',
        route: '/datasources',
    },
];

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
