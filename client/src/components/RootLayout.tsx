import { useMemo } from 'react';
import clsx from 'clsx';
import { Button, Navbar, NavbarContent, NavbarItem, Breadcrumbs as NextUIBreadcrumbs, BreadcrumbItem, Switch } from '@nextui-org/react';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { usePreferences, type Theme } from './PreferencesProvider';
import { Tooltip } from './common';
import { Sidebar } from './sidebar/Sidebar';
import { Link, Outlet, type UIMatch, useMatches } from 'react-router-dom';
import { cn } from './utils';

export function RootLayout() {
    const { theme, isCollapsed } = usePreferences().preferences;

    return (
        <div className={cn('h-screen overflow-hidden text-foreground bg-background', theme)}>
            <div className='flex h-full'>
                <Sidebar />
                <div
                    className={cn(
                        'flex flex-col flex-grow overflow-hidden transition-all duration-200',
                        isCollapsed ? 'ml-16' : 'ml-64',
                    )}
                >
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

type BreadcrumbData = {
    path: string;
    label: string;
};

type BreadcrumbMatch<TData> = UIMatch<TData, { breadcrumb: string | ((data: TData) => string) }>;

function Breadcrumbs() {
    const matches = useMatches();

    const breadcrumbs: BreadcrumbData[] = useMemo(() => {
        return matches.
            filter((match): match is BreadcrumbMatch<unknown> => !!(match.handle && typeof match.handle === 'object' && 'breadcrumb' in match.handle))
            .map(match => ({
                path: match.pathname,
                label: typeof match.handle.breadcrumb === 'function' ? match.handle.breadcrumb(match.data) : match.handle.breadcrumb,
            }));
    }, [ matches ]);

    return (
        <NextUIBreadcrumbs separator='/'>
            {breadcrumbs.map((crumb, index) => (
                <BreadcrumbItem key={crumb.path} isCurrent={index === breadcrumbs.length - 1}>
                    <Link to={crumb.path} className='text-danger-500'> {crumb.label} </Link>
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

type ShowTableIDsSwitchProps = {
    className?: string;
};

export function ShowTableIDsSwitch({ className }: ShowTableIDsSwitchProps) {
    const { preferences, setPreferences } = usePreferences();
    const { showTableIDs } = preferences;

    const handleChange = (isChecked: boolean) => {
        setPreferences({ ...preferences, showTableIDs: isChecked });
    };

    return (
        <div className={className}>
            <Switch
                isSelected={showTableIDs}
                onChange={(e) => handleChange(e.target.checked)}
                size='sm'
            >
                <p className='text-small'>Show Table IDs</p>
            </Switch>
        </div>
    );
}