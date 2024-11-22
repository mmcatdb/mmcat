import { useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { routes } from '@/routes/routes';
import { sidebarIconMap } from '@/components/icons/Icons';
import { Bars3Icon, XMarkIcon } from '@heroicons/react/24/outline';
import { ShowTableIDsSwitch } from '../RootLayout';

type SidebarItem = {
    label: string;
    route?: string;
    iconName?: keyof typeof sidebarIconMap;
    isSeparator?: boolean;   // because of this separator all other options (except label) may be optional
};

const generalSidebarItems: SidebarItem[] = [
    {
        label: 'Schema categories',
        route: '/schema-categories',
        iconName: 'heart',
    },
    {
        label: 'About',
        route: '/about',
        iconName: 'lightBulb',
    },
    {
        label: 'Datasources',
        route: '/datasources',
        iconName: 'circleStack',
    },
    {
        label: 'Adminer',
        route: '/adminer',
        iconName: 'codeBracketSquare',
    },
];

export function Sidebar() {
    const location = useLocation();
    const { categoryId } = useParams<'categoryId'>();
    const [ isSidebarOpen, setIsSidebarOpen ] = useState(false);

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    // project is not open, show the general sidebar items
    let dynamicSidebarItems: SidebarItem[] = generalSidebarItems;

    // If categoryId exists, replace the items with project-specific ones
    if (categoryId) {
        dynamicSidebarItems = [
            // Static separator 'Project'
            { label: 'Schema Category', isSeparator: true },
            {
                label: 'Overview',
                route: routes.category.index.resolve({ categoryId }),
                iconName: 'documentText',
            },
            {
                label: 'Editor',
                route: routes.category.editor.resolve({ categoryId }),
                iconName: 'documentText',
            },
            {
                label: 'Querying',
                route: routes.category.querying.resolve({ categoryId }),
                iconName: 'documentText',
            },
            {
                label: 'Datasources',
                route: routes.category.datasources.resolve({ categoryId }),
                iconName: 'documentText',
            },
        ];
    }

    return (
        <div className='relative'>
            {/* Sidebar button for mobiles */}
            <div className='lg:hidden p-4 fixed z-50'>
                <button onClick={toggleSidebar}>
                    <Bars3Icon className='w-8 h-8 text-zinc-500' />
                </button>
            </div>

            {/* Sidebar */}
            <div
                className={`fixed inset-y-0 left-0 transform bg-white dark:bg-black border-r border-zinc-200 dark:border-zinc-800 p-5 w-64 h-screen z-50 transition-transform duration-300 ease-in-out ${
                    isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
                } lg:relative lg:translate-x-0 lg:w-64 lg:h-screen lg:block`}
            >
                <button className='absolute top-4 right-4 lg:hidden' onClick={toggleSidebar}>
                    <XMarkIcon className='w-6 h-6 text-zinc-500' />
                </button>

                <Link to={routes.home.path} onClick={() => setIsSidebarOpen(false)}>
                    <h1 className='text-xl font-semibold mb-10'>MM-cat</h1>
                </Link>

                <div className='flex flex-col'>
                    {dynamicSidebarItems.map((item) => {
                        if (item.isSeparator) {
                            return (
                                <p
                                    key={`separator-${item.label}`}
                                    className='font-semibold px-2 py-2'
                                >
                                    {item.label}
                                </p>
                            );
                        }

                        // const isActive = item.route === '/' ? location.pathname === item.route : location.pathname.startsWith(item.route);
                        const isActive = item.route === location.pathname;
                        const icon = item.iconName ? sidebarIconMap[item.iconName] : null;

                        return (
                            <Link
                                key={item.route}
                                to={item.route ?? '#'}
                                onClick={() => setIsSidebarOpen(false)}
                                className={`flex items-center px-2 py-2 rounded hover:bg-zinc-100 dark:hover:bg-zinc-900 transition duration-200 ${
                                    isActive ? 'text-blue-600 font-semibold' : ''
                                }`}
                            >
                                {icon && (isActive ? icon.solid : icon.outline)}
                                {item.label}
                            </Link>
                        );
                    })}
                </div>
                <div className='absolute bottom-5 left-5 w-full'>
                    <ShowTableIDsSwitch />
                </div>
            </div>

            {isSidebarOpen && (
                <div
                    className='fixed inset-0 bg-black opacity-30 z-40 lg:hidden'
                    onClick={toggleSidebar}
                ></div>
            )}
        </div>
    );
}
