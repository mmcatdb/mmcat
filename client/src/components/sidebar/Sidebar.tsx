import { useState } from 'react';
import { Link, matchPath, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Switch, Tooltip } from '@nextui-org/react';
import { routes } from '@/routes/routes';
import { SidebarIconKey, sidebarIconMap } from '@/components/icons/Icons';
import { usePreferences } from '../PreferencesProvider';
import { CollapseContextToggle } from '@/components/sidebar/CollapseContextToggle';
import { cn } from '@/components/utils';
import { Cog6ToothIcon } from '@heroicons/react/24/outline';
import { PiCat } from 'react-icons/pi';

/**
 * Type for navigation items in the sidebar.
 */
type NormalSidebarItem = {
    type: 'normal';
    label: string;
    route: string;
    iconName: keyof typeof sidebarIconMap;
    match?: string[];
};

/**
 * This is a special type of item that is only used to separate other items.
 * It does not have a route or icon.
 */
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
                'fixed h-screen z-10 transition-all duration-300 ease-in-out border-r border-default-200',
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

/**
 * Display the settings button in the sidebar and modal for updating user preferences.
 */
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

type ShowTableIDsSwitchProps = {
    className?: string;
};

/**
 * Renders a switch to toggle visibility of table IDs.
 * It is a user preference updated in Settings.
 */
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
                onChange={e => handleChange(e.target.checked)}
                size='sm'
            >
                <p className='text-small'>Show Table IDs</p>
            </Switch>
        </div>
    );
}

/**
 * A link to the home page of the application.
 */
function SidebarHeader({ isCollapsed }: { isCollapsed: boolean }) {
    const content = (
        <Link
            to={routes.home.path}
            className={cn(
                'flex items-center mb-6 mt-2 pl-5 group',
                'transition-all duration-300 ease-in-out',
            )}
        >
            <div className={cn(
                'flex-shrink-0 text-foreground pr-2',
                'transition-transform duration-300 group-hover:rotate-6 group-hover:scale-110',
                isCollapsed ? 'w-8 h-8 text-xl' : 'w-8 h-8 text-2xl',
            )}>
                <PiCat className='w-full h-full' />
            </div>

            <h1 className={cn(
                'text-2xl whitespace-nowrap overflow-hidden',
                'transition-all duration-300 ease-in-out',
                isCollapsed ? 'w-0 opacity-0' : 'w-auto opacity-100',
            )}>
                <span className='inline-block transition-transform duration-300 group-hover:translate-y-[-1px] font-medium'>
                    MM-cat
                </span>
            </h1>
        </Link>
    );

    return isCollapsed ? (
        <Tooltip content='Home' placement='right' showArrow>
            {content}
        </Tooltip>
    ) : content;
}

/**
 * Renders a single sidebar item (normal link or separator).
 */
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
                className={cn('flex items-center px-3 py-3 mx-2 rounded-md hover:bg-default-100',
                    isActive ? 'text-primary-500 font-bold' : '',
                    theme === 'dark' ? 'hover:bg-zinc-900' : 'hover:bg-zinc-100', // needs to be defined via 'theme ===' not via default colors (HeroUI does not have good contrast in dark mode or light mode)
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

/**
 * Generates sidebar items for general navigation (no schema category context).
 */
function generalSidebarItems(): SidebarItem[] {
    return [
        {
            type: 'normal',
            label: 'Schema categories',
            route: routes.categories,
            iconName: SidebarIconKey.SchemaCategory,
        },
        {
            type: 'normal',
            label: 'Datasources',
            route: routes.datasources.path,
            iconName: SidebarIconKey.Datasources,
            match: [ routes.datasourceRoutes.datasource.path ],
        },
        // Work of other colleague, left here for future merge
        // {
        //     type: 'normal',
        //     label: 'Adminer',
        //     route: `${routes.adminer}?reload=true`,
        //     iconName: 'codeBracketSquare',
        // },
    ];
}

/**
 * Generates sidebar items for category-specific navigation.
 */
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
            iconName: SidebarIconKey.Overview,
        },
        {
            type: 'normal',
            label: 'Editor',
            route: routes.category.editor.resolve({ categoryId }),
            iconName: SidebarIconKey.Editor,
        },
        {
            type: 'normal',
            label: 'Datasources',
            route: routes.category.datasources.resolve({ categoryId }),
            iconName: SidebarIconKey.Datasources,
            match: [ routes.category.datasource.path ],
        },
        {
            type: 'normal',
            label: 'Actions',
            route: routes.category.actions.resolve({ categoryId }),
            iconName: SidebarIconKey.Rocket,
            match: [ routes.category.action.path, routes.category.addAction.path ],
        },
        {
            type: 'normal',
            label: 'Jobs',
            route: routes.category.jobs.resolve({ categoryId }),
            iconName: SidebarIconKey.PlayCircle,
            match: [ routes.category.job.path ],
        },
    ];
}
