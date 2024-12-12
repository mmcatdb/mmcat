import { Link, matchPath, useLocation, useParams } from 'react-router-dom';
import { routes } from '@/routes/routes';
import { sidebarIconMap } from '@/components/icons/Icons';
import { ShowTableIDsSwitch } from '../RootLayout';
import { usePreferences } from '../PreferencesProvider';
import { CollapseContextToggle } from '@/components/project/context';
import { Tooltip } from '@nextui-org/react';
import { cn } from '@/components/utils';

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
    const location = useLocation();
    const { categoryId } = useParams<'categoryId'>();
    const { theme, isCollapsed } = usePreferences().preferences;

    const dynamicSidebarItems: SidebarItem[] = categoryId
        ? categorySidebarItems(categoryId)
        : generalSidebarItems();

    return (
        <div
            className={cn('fixed border-r h-screen z-10 transition-all duration-300 ease-in-out',
                theme === 'dark' ? 'border-zinc-800' : 'border-zinc-200',
                isCollapsed ? 'w-16' : 'w-64',
            )}
        >
            <SidebarHeader isCollapsed={isCollapsed} />

            <div className='px-3 py-2'>
                <CollapseContextToggle />
            </div>

            <div className='flex flex-col'>
                {dynamicSidebarItems.map((item) => (
                    <SidebarItemDisplay key={item.label} item={item} currentPath={location.pathname} />
                    
                ))}
            </div>

            <div className='absolute bottom-5 left-3 w-full'>
                <ShowTableIDsSwitch />
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

function SidebarItemDisplay({ 
    item,
    currentPath,
}: {
    item : SidebarItem;
    currentPath: string;
}) {
    const { theme, isCollapsed } = usePreferences().preferences;

    switch (item.type) {
    case 'separator':
        return (
            <p
                key={`separator-${item.label}`}
                className={`font-semibold px-4 py-3 transition-all whitespace-nowrap duration-300 overflow-hidden`}
            >
                {isCollapsed ? item.collapsedLabel : item.label}
            </p>
        );
            
    case 'normal': {
        const isMatched = item.match?.some(path => matchPath(path, currentPath));
        const isActive = item.route === currentPath || isMatched;
        const icon = sidebarIconMap[item.iconName];

        const linkContent = (
            <Link
                key={item.route}
                to={item.route}
                className={cn('flex items-center px-3 py-3 mx-2 rounded-md',
                    isActive ? 'text-blue-600 font-semibold' : '',
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
    return (
        [
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
                route: routes.adminer,
                iconName: 'codeBracketSquare',
            },
        ]
    );
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
