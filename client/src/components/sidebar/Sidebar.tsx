import { type FunctionComponent, type SVGProps, useMemo, useState } from 'react';
import { Link, matchPath, useLocation, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, Switch, Tooltip } from '@heroui/react';
import { routes } from '@/routes/routes';
import { usePreferences } from '../context/PreferencesProvider';
import { CollapseContextToggle } from '@/components/sidebar/CollapseContextToggle';
import { Cog6ToothIcon } from '@heroicons/react/24/outline';
import { PiCat } from 'react-icons/pi';
import { cn } from '@/components/common/utils';
import { type Id } from '@/types/id';
import {
    SquaresPlusIcon as SquaresPlusOutline,
    CircleStackIcon as CircleStackOutline,
    CodeBracketSquareIcon as CodeBracketSquareOutline,
    PencilSquareIcon as PencilSquareOutline,
    RocketLaunchIcon as RocketLaunchOutline,
    PlayCircleIcon as PlayCircleOutline,
    DocumentTextIcon as DocumentTextOutline,
    MagnifyingGlassIcon as MagnifyingGlassOutline,
    ArrowPathIcon as ArrowPathOutline,
} from '@heroicons/react/24/outline';
import {
    SquaresPlusIcon as SquaresPlusSolid,
    CircleStackIcon as CircleStackSolid,
    CodeBracketSquareIcon as CodeBracketSquareSolid,
    PencilSquareIcon as PencilSquareSolid,
    RocketLaunchIcon as RocketLaunchSolid,
    PlayCircleIcon as PlayCircleSolid,
    DocumentTextIcon as DocumentTextSolid,
    MagnifyingGlassIcon as MagnifyingGlassSolid,
    ArrowPathIcon as ArrowPathSolid,
} from '@heroicons/react/24/solid';
import { MdDashboard, MdOutlineDashboard } from 'react-icons/md';

/**
 * Type for navigation items in the sidebar.
 */
type NormalMenuItem = {
    type: 'normal';
    label: string;
    solidIcon: FunctionComponent<SVGProps<SVGSVGElement>>;
    outlineIcon: FunctionComponent<SVGProps<SVGSVGElement>>;
    route: string;
    match?: string[];
};

/**
 * This is a special type of item that is only used to separate other items.
 * It does not have a route or icon.
 */
type SeparatorMenuItem = {
    type: 'separator';
    label: string;
    collapsedLabel: string;
};

type MenuItem = NormalMenuItem | SeparatorMenuItem;

export function Sidebar() {
    const { categoryId } = useParams<'categoryId'>();
    const { theme, isCollapsed } = usePreferences().preferences;

    const dynamicMenuItems: MenuItem[] = useMemo(() => categoryId ? categoryMenuItems(categoryId) : generalMenuItems(), [ categoryId ]);

    return (
        <div className={cn('fixed h-screen z-10 transition-all duration-300 ease-in-out border-r border-default-200', isCollapsed ? 'w-16' : 'w-64')}>
            <SidebarHeader isCollapsed={isCollapsed} />

            <div className='px-3 py-2'>
                <CollapseContextToggle />
            </div>

            <div className='flex flex-col'>
                {dynamicMenuItems.map(item => (
                    <MenuItemDisplay key={item.label} item={item} />
                ))}
            </div>

            <div className='absolute bottom-4'>
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
            className={cn('flex items-center px-3 py-3 mx-2 rounded-md', theme === 'dark' ? 'hover:bg-zinc-900' : 'hover:bg-zinc-100')}
        >
            <Cog6ToothIcon className='size-6' />
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
                    <SettingsForm />
                </ModalBody>
                <ModalFooter>
                    <Button onPress={() => setIsSettingsOpen(false)} color='primary'>Close</Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    </>);
}

function SettingsForm() {
    const { preferences, setPreferences } = usePreferences();

    return (
        <div className='flex flex-col gap-2'>
            <Switch
                isSelected={preferences.showTableIDs}
                onValueChange={value => setPreferences({ ...preferences, showTableIDs: value })}
                size='sm'
            >
                Show table IDs
            </Switch>

            <Switch
                isSelected={preferences.accessPathShortForm}
                onValueChange={value => setPreferences({ ...preferences, accessPathShortForm: value })}
                size='sm'
            >
                Short form of access path
            </Switch>

            <Switch
                isSelected={preferences.adminerShortLinks}
                onValueChange={value => setPreferences({ ...preferences, adminerShortLinks: value })}
                size='sm'
            >
                Short links in Adminer
            </Switch>
        </div>
    );
}

/**
 * A link to the home page of the application.
 */
function SidebarHeader({ isCollapsed }: { isCollapsed: boolean }) {
    const content = (
        <Link to={routes.home.path} className='flex items-center mb-6 mt-2 pl-5 group transition-all duration-300 ease-in-out'>
            <div className={cn('shrink-0 size-8 text-foreground pr-2 transition-transform duration-300 group-hover:rotate-6 group-hover:scale-110',
                isCollapsed ? 'text-xl' : 'text-2xl',
            )}>
                <PiCat className='w-full h-full' />
            </div>

            <h1 className={cn('text-2xl whitespace-nowrap overflow-hidden transition-all duration-300 ease-in-out',
                isCollapsed ? 'w-0 opacity-0' : 'w-auto opacity-100',
            )}>
                <span className='inline-block transition-transform duration-300 group-hover:-translate-y-px font-medium'>
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
 * Renders a single menu item (normal link or separator).
 */
function MenuItemDisplay({ item }: {
    item : MenuItem;
}) {
    const { theme, isCollapsed } = usePreferences().preferences;
    const location = useLocation();

    switch (item.type) {
    case 'separator':
        return (
            <p key={`separator-${item.label}`} className='font-semibold px-4 py-3 whitespace-nowrap overflow-hidden'>
                {isCollapsed ? item.collapsedLabel : item.label}
            </p>
        );

    case 'normal': {
        const isActive = !!matchPath(item.route, location.pathname) || !!item.match?.some(path => matchPath(path, location.pathname));

        const Icon = isActive ? item.solidIcon : item.outlineIcon;

        const linkContent = (
            <Link
                key={item.route}
                to={item.route}
                className={cn('flex items-center px-3 py-3 mx-2 rounded-md',
                    isActive && 'text-primary-500 font-bold',
                    theme === 'dark' ? 'hover:bg-zinc-900' : 'hover:bg-zinc-100', // needs to be defined via 'theme ===' not via default colors (HeroUI does not have good contrast in dark mode or light mode)
                )}
            >
                <Icon className='shrink-0 mr-2 size-6' />

                <span className={cn('ml-2 whitespace-nowrap overflow-hidden', isCollapsed ? 'w-0 opacity-0' : 'w-auto opacity-100')}>
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
    }
}

/**
 * Generates menu items for general navigation (no schema category context).
 */
function generalMenuItems(): MenuItem[] {
    return [ {
        type: 'normal',
        label: 'Schema categories',
        solidIcon: SquaresPlusSolid,
        outlineIcon: SquaresPlusOutline,
        route: routes.categories,
    }, {
        type: 'normal',
        label: 'Datasources',
        solidIcon: CircleStackSolid,
        outlineIcon: CircleStackOutline,
        route: routes.datasources.list.path,
        match: [ routes.datasources.detail.path ],
    }, {
        type: 'normal',
        label: 'Adminer',
        solidIcon: CodeBracketSquareSolid,
        outlineIcon: CodeBracketSquareOutline,
        route: `${routes.adminer}?reload=true`,
    } ];
}

/**
 * Generates menu items for category-specific navigation.
 */
function categoryMenuItems(categoryId: Id): MenuItem[] {
    return [ {
        type: 'separator',
        label: 'Schema Category',
        collapsedLabel: 'SC',
    }, {
        type: 'normal',
        label: 'Overview',
        solidIcon: MdDashboard,
        outlineIcon: MdOutlineDashboard,
        route: routes.category.index.resolve({ categoryId }),
    }, {
        type: 'normal',
        label: 'Editor',
        solidIcon: PencilSquareSolid,
        outlineIcon: PencilSquareOutline,
        route: routes.category.editor.resolve({ categoryId }),
    }, {
        type: 'normal',
        label: 'Datasources',
        solidIcon: CircleStackSolid,
        outlineIcon: CircleStackOutline,
        route: routes.category.datasources.list.resolve({ categoryId }),
        match: [ routes.category.datasources.detail.path, routes.category.mapping.path, routes.category.datasources.newMapping.path ],
    }, {
        type: 'normal',
        label: 'Actions',
        solidIcon: RocketLaunchSolid,
        outlineIcon: RocketLaunchOutline,
        route: routes.category.actions.list.resolve({ categoryId }),
        match: [ routes.category.actions.new.path ],
    }, {
        type: 'normal',
        label: 'Jobs',
        solidIcon: PlayCircleSolid,
        outlineIcon: PlayCircleOutline,
        route: routes.category.jobs.resolve({ categoryId }),
        match: [ routes.category.job.path ],
    }, {
        type: 'normal',
        label: 'Files',
        solidIcon: DocumentTextSolid,
        outlineIcon: DocumentTextOutline,
        route: routes.category.files.list.resolve({ categoryId }),
        match: [ routes.category.files.detail.path ],
    }, {
        type: 'normal',
        label: 'Querying',
        solidIcon: MagnifyingGlassSolid,
        outlineIcon: MagnifyingGlassOutline,
        route: routes.category.queries.list.resolve({ categoryId }),
        match: [ routes.category.queries.detail.path, routes.category.queries.new.path ],
    }, {
        type: 'normal',
        label: 'Adaptation',
        solidIcon: ArrowPathSolid,
        outlineIcon: ArrowPathOutline,
        route: routes.category.adaptation.resolve({ categoryId }),
        match: [ routes.category.adaptation.path ],
    } ];
}
