import { useMemo } from 'react';
import { Button, Navbar, Breadcrumbs as HeroUIBreadcrumbs, BreadcrumbItem, Dropdown, DropdownTrigger, DropdownMenu, DropdownItem } from '@heroui/react';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { usePreferences } from './PreferencesProvider';
import { Tooltip } from './common';
import { Sidebar } from './sidebar/Sidebar';
import { Link, Outlet, type UIMatch, useMatches } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import { IoBookOutline, IoFolderOpenSharp, IoHelpSharp } from 'react-icons/io5';
import { FaGithub } from 'react-icons/fa';
import { useEffect } from 'react';
import { twJoin, twMerge } from 'tailwind-merge';

/**
 * The main layout of the application.
 * It contains the sidebar, navbar, and main content area.
 */
export function RootLayout() {
    const { isCollapsed } = usePreferences().preferences;
    const location = useLocation(); // Get current route

    // A workaround to detect if we are on the editor page and adjust the layout accordingly.
    const isFullPage = location.pathname.includes('/editor') || location.pathname.includes('/mappings') || location.pathname.includes('/adminer');

    return (<>
        <ScrollToTop />
        <div className='h-screen overflow-hidden text-foreground bg-background'>
            <div className='flex h-full'>
                <Sidebar />
                <div className={twJoin('flex flex-col grow transition-all duration-300', isCollapsed ? 'ml-16' : 'ml-64')}>
                    <CommonNavbar />
                    <main className='grow relative'>
                        <div className={twJoin('absolute inset-0', isFullPage ? 'grow h-full overflow-hidden' : 'overflow-y-auto')}>
                            <div className={twJoin('relative grow mx-auto', isFullPage ? 'h-full' : 'max-w-5xl p-6 overflow-y-auto')}>
                                <Outlet />
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        </div>
    </>);
}

function CommonNavbar() {
    return (
        <Navbar className='z-20 w-full h-10 border-b border-default-200' isBlurred={false} maxWidth='full'>
            <div className='flex items-center h-full w-full'>
                <div className='hidden lg:flex lg:flex-1 items-center overflow-hidden'>
                    <Breadcrumbs />
                </div>

                <div className='flex items-center shrink-0 gap-2 ml-auto'>
                    {/* The documentation for help is not ready yet, so displaying just GitHub link. *}
                    {/* <HelpDropdown /> */}
                    <Tooltip content='GitHub Repository' placement='bottom'>
                        <Button
                            isIconOnly
                            aria-label='GitHub Repository'
                            variant='light'
                            className='w-7 h-7 min-w-6'
                            as='a'
                            href='https://github.com/mmcatdb/mmcat/tree/ui'
                            target='_blank'
                        >
                            <FaGithub size={20} />
                        </Button>
                    </Tooltip>
                    <ThemeToggle className='w-7 h-7' />
                </div>
            </div>
        </Navbar>
    );
}

/**
 * Resets scroll position on route changes.
 */
function ScrollToTop() {
    const { pathname } = useLocation();

    useEffect(() => {
        const timer = setTimeout(() => {
            // Try window scroll first
            window.scrollTo(0, 0);
            // Target the scrollable container in RootLayout, without this it won't work
            // because the scrollable container is not the whole window
            const scrollable = document.querySelector('.overflow-y-auto');
            if (scrollable)
                scrollable.scrollTop = 0;

        }, 0);

        return () => clearTimeout(timer);
    }, [ pathname ]);

    return null;
}

type BreadcrumbData = {
    path: string;
    label: string;
};

type BreadcrumbMatch<TData> = UIMatch<TData, { breadcrumb: string | ((data: TData) => string) }>;

/**
 * Renders breadcrumbs based on route matches.
 */
function Breadcrumbs() {
    const matches = useMatches();

    const breadcrumbs: BreadcrumbData[] = useMemo(() => {
        return matches
            .filter((match): match is BreadcrumbMatch<unknown> =>
                !!(match.handle && typeof match.handle === 'object' && 'breadcrumb' in match.handle))
            .map(match => ({
                path: match.pathname,
                label: typeof match.handle.breadcrumb === 'function'
                    ? match.handle.breadcrumb(match.data)
                    : match.handle.breadcrumb,
            }));
    }, [ matches ]);

    return (
        <HeroUIBreadcrumbs
            separator={
                <span className='mx-1 text-default-400 select-none'>/</span>
            }
            className='px-2 py-1'
        >
            {breadcrumbs.map((crumb, index) => {
                const isCurrent = index === breadcrumbs.length - 1;
                return (
                    <BreadcrumbItem key={crumb.path} isCurrent={isCurrent}>
                        <Link
                            to={crumb.path}
                            className={twJoin('truncate max-w-[160px] focus-visible:outline-hidden',
                                isCurrent ? 'font-semibold text-foreground hover:text-default-800' : 'text-default-800 hover:text-default-700',
                            )}
                            title={crumb.label}
                        >
                            {crumb.label}
                        </Link>
                    </BreadcrumbItem>
                );
            })}
        </HeroUIBreadcrumbs>
    );
}

/**
 * A dropdown menu with help related links.
 */
// This is a placeholder for now, as the documentation is not ready yet.
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function HelpDropdown() {
    return (
        <Dropdown>
            <DropdownTrigger>
                <Button isIconOnly aria-label='Help' variant='light' className='w-7 h-7 min-w-6'>
                    <Tooltip content='Help' placement='bottom'>
                        <span>
                            <IoHelpSharp size={18} />
                        </span>
                    </Tooltip>
                </Button>
            </DropdownTrigger>
            <DropdownMenu aria-label='Help Links'>
                <DropdownItem
                    key='user-docs'
                    as='a'
                    href='https://mmcatdb.com/user-guide/'
                    target='_blank'
                    startContent={<IoBookOutline size={24} />}
                    description='Read the user guide to learn how to use the app.'
                    variant='flat'
                >
                    User Guide →
                </DropdownItem>
                <DropdownItem
                    key='github'
                    as='a'
                    href='https://github.com/mmcatdb/mmcat'
                    target='_blank'
                    startContent={<FaGithub size={24} />}
                    description='Find source code, report issues, and contribute.'
                    variant='flat'
                >
                    GitHub Repository →
                </DropdownItem>
                <DropdownItem
                    key='tree-structure'
                    as='a'
                    href='https://mmcatdb.com/'
                    target='_blank'
                    startContent={<IoFolderOpenSharp size={24} />}
                    description='Explore the app structure, docs, and motivation.'
                    variant='flat'
                >
                    Project Overview →
                </DropdownItem>
            </DropdownMenu>
        </Dropdown>
    );
}

/**
 * A button to toggle between light and dark themes.
 */
export function ThemeToggle({ className }: { className?: string }) {
    const { preferences, setPreferences } = usePreferences();
    const { theme } = preferences;
    const nextValue = theme === 'light' ? 'dark' : 'light';
    const label = `Switch to ${nextValue} theme`;

    return (
        <Tooltip content={label}>
            <Button
                isIconOnly
                aria-label={label}
                onPress={() => setPreferences({ ...preferences, theme: nextValue })}
                variant='light'
                className={twMerge('w-6 h-6 min-w-6 p-0', className)}
            >
                {theme === 'dark' ? (
                    <MdOutlineDarkMode size={18} />
                ) : (
                    <MdOutlineLightMode size={18} />
                )}
            </Button>
        </Tooltip>
    );
}
