import { useMemo } from 'react';
import { Button, Navbar, Breadcrumbs as NextUIBreadcrumbs, BreadcrumbItem, Dropdown, DropdownTrigger, DropdownMenu, DropdownItem } from '@nextui-org/react';
import { MdOutlineDarkMode, MdOutlineLightMode } from 'react-icons/md';
import { usePreferences } from './PreferencesProvider';
import { Tooltip } from './common';
import { Sidebar } from './sidebar/Sidebar';
import { Link, Outlet, type UIMatch, useMatches } from 'react-router-dom';
import { cn } from './utils';
import { useLocation } from 'react-router-dom';
import { IoBookOutline, IoFolderOpenSharp, IoHelpSharp } from 'react-icons/io5';
import { FaGithub } from 'react-icons/fa';

export function RootLayout() {
    const { isCollapsed } = usePreferences().preferences;
    const location = useLocation(); // Get current route

    // A workaround to detect if we are on the editor page and adjust the layout accordingly.
    const isEditorPage = location.pathname.includes('/editor');

    return (
        <div className={cn('h-screen overflow-hidden text-foreground bg-background')}>
            <div className='flex h-full'>
                <Sidebar />
                <div className={cn(
                    'flex flex-col flex-grow transition-all duration-300',
                    isCollapsed ? 'ml-16' : 'ml-64',
                )}>
                    <CommonNavbar />
                    <main className='flex-grow relative'>
                        <div className={cn('absolute inset-0', isEditorPage ? 'overflow-hidden flex-grow h-full' : 'overflow-y-auto')}>
                            <div className={cn(
                                'relative flex-grow mx-auto',
                                isEditorPage ? 'h-full' : 'max-w-5xl p-6 overflow-y-auto',
                            )}>
                                <Outlet />
                                {/* <SchemaCategoryEditor /> */}
                            </div>
                        </div>
                    </main>
                </div>
            </div>
        </div>
    );
}

function CommonNavbar() {
    return (
        <Navbar className='z-20 w-full h-10 border-b border-default-200' isBlurred={false} maxWidth='full'>
            <div className='flex items-center h-full w-full'>
                <div className='flex flex-1 items-center overflow-hidden'>
                    <Breadcrumbs />
                </div>

                <div className='flex items-center shrink-0 gap-2 ml-4'>
                    <HelpDropdown />
                    <ThemeToggle className='w-7 h-7' />
                </div>
            </div>
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
            {breadcrumbs.map((crumb, index) => {
                const isCurrent = index === breadcrumbs.length - 1;
                return (
                    <BreadcrumbItem key={crumb.path} isCurrent={isCurrent}>
                        <Link
                            to={crumb.path}
                            className={cn(`truncate max-w-[160px] ${
                                isCurrent ? 'font-semibold text-secondary-500' : 'font-medium text-secondary-500'
                            }`)}
                            title={crumb.label}
                        >
                            {crumb.label}
                        </Link>
                    </BreadcrumbItem>
                );
            })}
        </NextUIBreadcrumbs>
    );
}

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

type ThemeToggleProps = Readonly<{
    className?: string;
}>;

export function ThemeToggle({ className }: ThemeToggleProps) {
    const { preferences, setPreferences } = usePreferences();
    const { theme } = preferences;
    const nextValue = theme === 'dark' ? 'light' : 'dark';
    const label = `Switch to ${nextValue} theme`;

    return (
        <Tooltip content={label}>
            <Button
                isIconOnly
                aria-label={label}
                onPress={() => setPreferences({ ...preferences, theme: nextValue })}
                variant='light'
                className={cn('w-6 h-6 min-w-6 p-0', className)}
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
