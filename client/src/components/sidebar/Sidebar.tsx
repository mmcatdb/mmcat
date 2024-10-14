import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
    HomeIcon as HomeIconOutline,
    LightBulbIcon as LightBulbIconOutline,
    CircleStackIcon as CircleStackIconOutline,
    CodeBracketSquareIcon as CodeBracketSquareIconOutline,
    Bars3Icon,
    XMarkIcon,
} from '@heroicons/react/24/outline';
import {
    HomeIcon as HomeIconSolid,
    LightBulbIcon as LightBulbIconSolid,
    CircleStackIcon as CircleStackIconSolid,
    CodeBracketSquareIcon as CodeBracketSquareIconSolid,
} from '@heroicons/react/24/solid';

const sidebarItems = [
    {
        label: 'Home',
        route: '/',
        outlineIcon: <HomeIconOutline className='mr-2 w-5 h-5' />,
        solidIcon: <HomeIconSolid className='mr-2 w-5 h-5' />,
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
    const [ isSidebarOpen, setIsSidebarOpen ] = useState(false);

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    return (
        <div className='relative'>
            <div className='lg:hidden p-4 fixed z-50'>
                <button onClick={toggleSidebar}>
                    <Bars3Icon className='w-8 h-8 text-zinc-500' />
                </button>
            </div>

            <div
                className={`fixed inset-y-0 left-0 transform bg-white dark:bg-black border-r border-zinc-200 dark:border-zinc-800 p-6 w-64 h-screen z-50 transition-transform duration-300 ease-in-out ${
                    isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
                } lg:relative lg:translate-x-0 lg:w-64 lg:h-screen lg:block`}
            >
                <button
                    className='absolute top-4 right-4 lg:hidden'
                    onClick={toggleSidebar}
                >
                    <XMarkIcon className='w-6 h-6 text-zinc-500' />
                </button>

                <h1 className='text-xl font-semibold mb-10'>MM-cat</h1>

                <div className='flex flex-col'>
                    {sidebarItems.map((item) => {
                        // item stays active, including subpaths of the item (had to exclude / for Home)
                        const isActive =
                        item.route === '/'
                            ? location.pathname === item.route
                            : location.pathname.startsWith(item.route);
                        // const isActive = item.route === location.pathname;

                        return (
                            <Link
                                key={item.route}
                                to={item.route}
                                onClick={() => setIsSidebarOpen(false)}
                                className={`flex items-center px-2 py-1.5 rounded hover:bg-zinc-100 dark:hover:bg-zinc-900 transition duration-200 ${
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
