import { useState } from 'react';
import { Link, matchPath, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Tooltip } from '@nextui-org/react';
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
                <SettingsItemDisplay theme={theme} isCollapsed={isCollapsed} />
            </div>
        </div>
    );
}

function SettingsItemDisplay({ theme, isCollapsed }: { theme: string, isCollapsed: boolean }) {
    const [ isSettingsOpen, setIsSettingsOpen ] = useState(false);

    const openSettingsButton = (
        <button
            onClick={() => setIsSettingsOpen(true)}
            className={cn(
                'flex items-center px-3 py-3 mx-2 rounded-md',
                theme === 'dark' ? 'hover:bg-zinc-900' : 'hover:bg-zinc-100',
            )}
        >
            <Cog6ToothIcon className='w-6 h-6' />
            {!isCollapsed && <span className='px-4'>Settings</span>}
        </button>
    );

    return (<>
        {isCollapsed ? (
            <Tooltip content='Settings' placement='right' showArrow>
                {openSettingsButton}
            </Tooltip>
        ) : (
            openSettingsButton
        )}

        <Modal isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} placement='center'>
            <ModalContent>
                <ModalHeader>Settings</ModalHeader>
                <ModalBody>
                    <p>Customize your application preferences here.</p>
                    <ShowTableIDsSwitch />
                </ModalBody>
                <ModalFooter>
                    <Button onPress={() => setIsSettingsOpen(false)} color='primary'>Close</Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    </>);
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
