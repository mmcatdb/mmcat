import { Link, useLocation, useParams } from 'react-router-dom';
import { routes } from '@/routes/routes';
import { sidebarIconMap } from '@/components/icons/Icons';
import { ShowTableIDsSwitch } from '../RootLayout';
import { usePreferences } from '../PreferencesProvider';
import { CollapseContextToggle } from '@/components/project/context';

type NormalSidebarItem = {
    type: 'normal';
    label: string;
    route: string;
    iconName: keyof typeof sidebarIconMap;
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
    const { isCollapsed } = usePreferences().preferences;

    const dynamicSidebarItems: SidebarItem[] = categoryId
        ? SideBarItemsinCategory(categoryId)
        : generalSidebarItems;

    return (
        <div
            className={`fixed border-r border-zinc-200 dark:border-zinc-800 h-screen z-10 transition-all duration-300 ease-in-out ${
                isCollapsed ? 'w-16' : 'w-64'
            }`}
        >
            {/* Title (also link to Main page) */}
            <Link to={routes.home.path} className='flex items-center mb-6'>
                <h1
                    className={`text-xl font-semibold pt-2 pl-3 whitespace-nowrap overflow-hidden `}
                >
                    {isCollapsed ? 'MM' : 'MM-cat'}
                </h1>
            </Link>

            {/* Sidebar Items */}
            <div className='flex flex-col'>
                {dynamicSidebarItems.map((item) => {
                    if (item.type === 'separator') {
                        return (
                            <p
                                key={`separator-${item.label}`}
                                className={`font-semibold px-4 py-3 transition-all whitespace-nowrap duration-300 overflow-hidden`}
                            >
                                {isCollapsed ? item.collapsedLabel : item.label}
                            </p>
                        );
                    }

                    const isActive = item.route === location.pathname;
                    const icon = sidebarIconMap[item.iconName];

                    return (
                        <Link
                            key={item.route}
                            to={item.route}
                            className={`flex items-center px-3 py-3 mx-2 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-900 ${
                                isActive ? 'text-blue-600 font-semibold' : ''
                            }`}
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
                })}
            </div>

            <div className='px-3 py-2'>
                <CollapseContextToggle />
            </div>

            {/* Footer */}
            <div className='absolute bottom-5 left-5 w-full'>
                <ShowTableIDsSwitch />
            </div>
        </div>


    );
}

const generalSidebarItems: SidebarItem[] = [
    {
        type: 'normal',
        label: 'Schema categories',
        route: '/schema-categories',
        iconName: 'heart',
    },
    {
        type: 'normal',
        label: 'About',
        route: '/about',
        iconName: 'lightBulb',
    },
    {
        type: 'normal',
        label: 'Datasources',
        route: '/datasources',
        iconName: 'circleStack',
    },
    {
        type: 'normal',
        label: 'Adminer',
        route: '/adminer',
        iconName: 'codeBracketSquare',
    },
];

const SideBarItemsinCategory = (categoryId: string): SidebarItem[] => [
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
        label: 'Querying',
        route: routes.category.querying.resolve({ categoryId }),
        iconName: 'documentText',
    },
    {
        type: 'normal',
        label: 'Datasources',
        route: routes.category.datasources.resolve({ categoryId }),
        iconName: 'documentText',
    },
];
