import { useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import {
    HomeIcon as HomeIconOutline,
    HeartIcon as HeartIconOutline,
    CircleStackIcon as CircleStackIconOutline,
    CodeBracketSquareIcon as CodeBracketSquareIconOutline,
    LightBulbIcon as LightBulbIconOutline,
    Bars3Icon,
    XMarkIcon,
    DocumentTextIcon,
} from '@heroicons/react/24/outline';
import {
    HomeIcon as HomeIconSolid,
    HeartIcon as HeartIconSolid,
    CircleStackIcon as CircleStackIconSolid,
    CodeBracketSquareIcon as CodeBracketSquareIconSolid,
    LightBulbIcon as LightBulbIconSolid,
} from '@heroicons/react/24/solid';
import { routes } from '@/routes/routes';

type SidebarItem = {
    label: string;
    route?: string;
    outlineIcon?: JSX.Element;
    solidIcon?: JSX.Element;
    isSeparator?: boolean;   // all other options (except label) may be optional because of this separator
};

const generalSidebarItems: SidebarItem[] = [
    {
        label: 'Home',
        route: routes.home.path,
        outlineIcon: <HomeIconOutline className='mr-2 w-5 h-5' />,
        solidIcon: <HomeIconSolid className='mr-2 w-5 h-5' />,
    },
    {
        label: 'Schema categories',
        route: '/schema-categories',
        outlineIcon: <HeartIconOutline className='mr-2 w-5 h-5' />,
        solidIcon: <HeartIconSolid className='mr-2 w-5 h-5' />,
    },
    {
        label: 'About',
        route: '/about',
        outlineIcon: <LightBulbIconOutline className='mr-2 w-5 h-5' />,
        solidIcon: <LightBulbIconSolid className='mr-2 w-5 h-5' />,
    },
    {
        label: 'Datasources',
        route: '/datasources',
        outlineIcon: <CircleStackIconOutline className='mr-2 w-5 h-5' />,
        solidIcon: <CircleStackIconSolid className='mr-2 w-5 h-5' />,
    },
    {
        label: 'Adminer',
        route: '/adminer',
        outlineIcon: <CodeBracketSquareIconOutline className='mr-2 w-5 h-5' />,
        solidIcon: <CodeBracketSquareIconSolid className='mr-2 w-5 h-5' />,
    },
];

function Sidebar() {
    const location = useLocation();
    const { projectId } = useParams<'projectId'>();
    const [ isSidebarOpen, setIsSidebarOpen ] = useState(false);

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    // project is not open, show the general sidebar items
    let dynamicSidebarItems: SidebarItem[] = generalSidebarItems;

    // If projectId exists, replace the items with project-specific ones
    if (projectId) {
        dynamicSidebarItems = [
            {
                label: 'Home',
                route: routes.home.path,
                outlineIcon: <HomeIconOutline className='mr-2 w-5 h-5' />,
                solidIcon: <HomeIconSolid className='mr-2 w-5 h-5' />,
            },
            {
                label: 'Schema categories',
                route: '/schema-categories',
                outlineIcon: <HeartIconOutline className='mr-2 w-5 h-5' />,
                solidIcon: <HeartIconSolid className='mr-2 w-5 h-5' />,
            },
            // Static separator 'Project'
            { label: 'Project', isSeparator: true },
            {
                label: 'Schema category',
                route: routes.project.index.resolve({ projectId }), // Points to the project schema category
                outlineIcon: <DocumentTextIcon className='mr-2 w-5 h-5' />,
                solidIcon: <DocumentTextIcon className='mr-2 w-5 h-5' />,
            },
            {
                label: 'Models',
                route: routes.project.models.resolve({ projectId }),
                outlineIcon: <DocumentTextIcon className='mr-2 w-5 h-5' />,
                solidIcon: <DocumentTextIcon className='mr-2 w-5 h-5' />,
            },
            {
                label: 'Querying',
                route: routes.project.querying.resolve({ projectId }),
                outlineIcon: <DocumentTextIcon className='mr-2 w-5 h-5' />,
                solidIcon: <DocumentTextIcon className='mr-2 w-5 h-5' />,
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

                <h1 className='text-xl font-semibold mb-10'>MM-cat</h1>

                <div className='flex flex-col'>
                    {dynamicSidebarItems.map((item) => {
                        if (item.isSeparator) {
                            return (
                                <p
                                    key={`separator-${item.label}`}
                                    className='font-semibold px-2 py-2 text-sm'
                                >
                                    {item.label}
                                </p>
                            );
                        }

                        // const isActive = item.route === '/' ? location.pathname === item.route : location.pathname.startsWith(item.route);
                        const isActive = item.route === location.pathname;

                        return (
                            <Link
                                key={item.route}
                                to={item.route || '#'}
                                onClick={() => setIsSidebarOpen(false)}
                                className={`flex items-center px-2 py-2 text-sm rounded hover:bg-zinc-100 dark:hover:bg-zinc-900 transition duration-200 ${
                                    isActive ? 'text-blue-600 font-semibold' : ''
                                }`}
                            >
                                {isActive ? item.solidIcon : item.outlineIcon}
                                {item.label}
                            </Link>
                        );
                    })}
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

export default Sidebar;
