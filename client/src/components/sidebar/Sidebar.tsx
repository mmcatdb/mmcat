import { useState } from 'react';
import { Link, matchPath, useParams } from 'react-router-dom';
import { Button, Tooltip } from '@nextui-org/react';
import { routes } from '@/routes/routes';
import { sidebarIconMap } from '@/components/icons/Icons';
import { usePreferences } from '../PreferencesProvider';
import { CollapseContextToggle } from '@/components/CollapseContextToggle';
import { cn } from '@/components/utils';
import { ShowTableIDsSwitch } from '../RootLayout';
import { Cog6ToothIcon } from '@heroicons/react/24/outline';

type NormalSidebarItem = {
    type: 'normal';
    label: string;
    route: string;
    iconName: keyof typeof sidebarIconMap;
    match?: string[];
};

type SeparatorSidebarItem = {
    type: 'separator';
    label: string;
    collapsedLabel: string;
};

type SidebarItem = NormalSidebarItem | SeparatorSidebarItem;

export function Sidebar() {
    const { categoryId } = useParams<'categoryId'>();
    const { theme, isCollapsed } = usePreferences().preferences;
    const [ isSettingsOpen, setIsSettingsOpen ] = useState(false);

    const dynamicSidebarItems: SidebarItem[] = categoryId
        ? categorySidebarItems(categoryId)
        : generalSidebarItems();

    return (
        <div
            className={cn(
                'fixed border-r h-screen z-10 transition-all duration-300 ease-in-out',
                theme === 'dark' ? 'border-zinc-800' : 'border-zinc-200',
                isCollapsed ? 'w-16' : 'w-64',
            )}
        >
            <SidebarHeader isCollapsed={isCollapsed} />

            <div className='px-3 py-2'>
                <CollapseContextToggle />
            </div>

            <div className='flex flex-col'>
                {dynamicSidebarItems.map(item => (
                    <SidebarItemDisplay key={item.label} item={item} />
                ))}
            </div>

            <div className={cn('absolute bottom-4')}>
                <SettingsItemDisplay onOpen={() => setIsSettingsOpen(true)} />
            </div>

            <SettingsModal isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} />
        </div>
    );
}

function SettingsModal({ isOpen, onClose }: { isOpen: boolean, onClose: () => void }) {
    if (!isOpen) 
        return null;

    return (
        // Trying to imitate HeroUI modal
        // TBI: add trapping keyboard or change to modal, overlay all components
        <div className='fixed inset-0 flex items-center justify-center bg-black/50'>
            <div className='bg-white dark:bg-zinc-900 rounded-xl shadow-xl w-full max-w-md p-6 relative'>
                {/* Trying to imitate HeroUI modal close button */}
                <button 
                    onClick={onClose} 
                    className='absolute top-1 right-1 flex items-center justify-center w-7 h-7 rounded-full 
                            text-zinc-500 dark:text-zinc-400 dark:hover:text-zinc-300
                            hover:bg-zinc-100 dark:hover:bg-zinc-800'
                >
                    ✕
                </button>

                <h2 className='text-lg font-bold black pb-6'>Settings</h2>
                <p className='pb-2'>Customize your application preferences here.</p>
                <ShowTableIDsSwitch />
                <div className='flex justify-end pt-2'>
                    <Button onPress={onClose} color='primary'>Close</Button>
                </div>
            </div>
        </div>
    );
}

function SidebarHeader({ isCollapsed }: { isCollapsed: boolean })  {
    return (
        <Link to={routes.home.path} className='flex items-center mb-6'>
            <h1
                className='text-xl font-semibold pt-2 pl-3 whitespace-nowrap overflow-hidden'
            >
                {isCollapsed ? 'MM' : 'MM-cat'}
            </h1>
        </Link>
    );
}

function SidebarItemDisplay({ item }: {
    item : SidebarItem;
}) {
    const { theme, isCollapsed } = usePreferences().preferences;

    switch (item.type) {
    case 'separator':
        return (
            <p
                key={`separator-${item.label}`}
                className={`font-semibold px-4 py-3 whitespace-nowrap overflow-hidden`}
            >
                {isCollapsed ? item.collapsedLabel : item.label}
            </p>
        );

    case 'normal': {
        // Get clean pathname without query params
        const currentPathWithoutQuery = new URL(window.location.href).pathname;
        const itemRouteWithoutQuery = new URL(item.route, window.location.origin).pathname;

        const isMatched = item.match?.some(path => matchPath(path, currentPathWithoutQuery));
        const isActive = currentPathWithoutQuery === itemRouteWithoutQuery || isMatched;
        const icon = sidebarIconMap[item.iconName];

        const linkContent = (
            <Link
                key={item.route}
                to={item.route}
                className={cn('flex items-center px-3 py-3 mx-2 rounded-md',
                    isActive ? 'text-blue-500 font-bold' : '',
                    theme === 'dark' ? 'hover:bg-zinc-900' : 'hover:bg-zinc-100',
                )}
            >
                <span className='flex-shrink-0'>{icon && (isActive ? icon.solid : icon.outline)}</span>

                <span
                    className={`ml-2 whitespace-nowrap overflow-hidden ${
                        isCollapsed ? 'w-0 opacity-0' : 'w-auto opacity-100'
                    }`}
                >
                    {item.label}
                </span>
            </Link>
        );

        return isCollapsed ? (
            <Tooltip delay={200} closeDelay={0} key={item.label} placement='right' content={item.label} showArrow>
                {linkContent}
            </Tooltip>
        ) : (
            linkContent
        );
    }

    default:
        throw new Error(`Unhandled SidebarItem type: ${JSON.stringify(item)}`);
    }
}

function SettingsItemDisplay({ onOpen }: { onOpen: () => void }) {
    const { theme, isCollapsed } = usePreferences().preferences;

    const linkContent = (
        <button
            onClick={onOpen}
            className={cn(
                'flex items-center px-3 py-3 mx-2 rounded-md',
                theme === 'dark' ? 'hover:bg-zinc-900' : 'hover:bg-zinc-100',
            )}
        >
            <span className='flex-shrink-0'>
                <Cog6ToothIcon className=' w-6 h-6' />
            </span>

            <span
                className={`whitespace-nowrap  ${
                    isCollapsed ? 'w-0 opacity-0' : 'pl-4 pr-6 w-auto opacity-100'
                }`}
            >
                Settings
            </span>
        </button>
    );

    return isCollapsed ? (
        <Tooltip delay={200} closeDelay={0} key={'settings'} placement='right' content={'Settings'} showArrow>
            {linkContent}
        </Tooltip>
    ) : (
        linkContent
    );
}

function generalSidebarItems(): SidebarItem[] {
    return [
        {
            type: 'normal',
            label: 'Schema categories',
            route: routes.categories,
            iconName: 'heart',
        },
        {
            type: 'normal',
            label: 'About',
            route: routes.about,
            iconName: 'lightBulb',
        },
        {
            type: 'normal',
            label: 'Datasources',
            route: routes.datasources,
            iconName: 'circleStack',
            match: [ '/datasources/:id' ],
        },
        {
            type: 'normal',
            label: 'Adminer',
            route: `${routes.adminer}?reload=true`,
            iconName: 'codeBracketSquare',
        },
    ];
}

function categorySidebarItems(categoryId: string): SidebarItem[] {
    return [
        {
            type: 'separator',
            label: 'Schema Category',
            collapsedLabel: 'SC',
        },
        {
            type: 'normal',
            label: 'Overview',
            route: routes.category.index.resolve({ categoryId }),
            iconName: 'documentText',
        },
        {
            type: 'normal',
            label: 'Editor',
            route: routes.category.editor.resolve({ categoryId }),
            iconName: 'documentText',
        },
        {
            type: 'normal',
            label: 'Datasources',
            route: routes.category.datasources.resolve({ categoryId }),
            iconName: 'circleStack',
            match: [ routes.category.datasources.resolve({ categoryId }) + '/:id' ],
        },
        {
            type: 'normal',
            label: 'Actions',
            route: routes.category.actions.resolve({ categoryId }),
            iconName: 'rocket',
            match: [
                routes.category.actions.resolve({ categoryId }) + '/:id',
                routes.category.actions.resolve({ categoryId }) + '/add',
            ],
        },
        {
            type: 'normal',
            label: 'Jobs',
            route: routes.category.jobs.resolve({ categoryId }),
            iconName: 'playCircle',
            match: [ routes.category.jobs.resolve({ categoryId }) + '/:id' ],
        },
        {
            type: 'normal',
            label: 'Querying',
            route: routes.category.querying.resolve({ categoryId }),
            iconName: 'documentText',
        },
    ];
}
