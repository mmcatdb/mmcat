import clsx from 'clsx';
import { Button, Navbar, NavbarContent, NavbarItem } from '@nextui-org/react';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { usePreferences, type Theme } from './PreferencesProvider';
import { Tooltip } from './common';
import Sidebar from './sidebar/Sidebar';

type CommonPageProps = Readonly<{
    children: React.ReactNode;
}>;

export function CommonPage({ children }: CommonPageProps) {
    const { theme } = usePreferences().preferences;

    return (
        <div className={clsx('h-screen overflow-hidden text-foreground bg-background', theme)}>
            <div className='flex h-full'>
                <Sidebar />
                <div className='flex flex-col flex-grow overflow-hidden'>
                    <CommonNavbar />
                    <main className='flex-grow overflow-auto w-full max-w-[1024px] mx-auto p-6'>
                        {children}
                    </main>
                </div>
            </div>
        </div>
    );
}

function CommonNavbar() {
    return (
        <Navbar>
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
