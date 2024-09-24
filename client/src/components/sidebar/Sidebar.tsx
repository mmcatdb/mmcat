import { Link, useLocation } from 'react-router-dom';
import { HomeIcon } from '@heroicons/react/24/outline';
import { LightBulbIcon } from '@heroicons/react/24/outline';
import { CircleStackIcon } from '@heroicons/react/24/outline';
import { CodeBracketSquareIcon } from '@heroicons/react/24/outline';

const sidebarItems = [
    {
        label: 'Home',
        route: '/',
        icon: <HomeIcon className='mr-2 w-5 h-5' />,
    },
    {
        label: 'About',
        route: '/about',
        icon: <LightBulbIcon className='mr-2 w-5 h-5' />,
    },
    {
        label: 'Datasources',
        route: '/datasources',
        icon: <CircleStackIcon className='mr-2 w-5 h-5' />,
    },
    {
        label: 'Adminer',
        route: '/adminer',
        icon: <CodeBracketSquareIcon className='mr-2 w-5 h-5' />,
    },
];

function Sidebar() {
    const location = useLocation();

    return (
        <div className='w-64 h-screen p-6 flex flex-col border-r border-grey-300'>
            <h1 className='text-2xl font-semibold mb-10'>New Sidebar</h1>

            <div className='flex flex-col'>
                {sidebarItems.map((item) => {
                    const isActive = item.route === location.pathname;

                    return (
                        <Link
                            key={item.route}
                            to={item.route}
                            className={`flex items-center px-4 py-2 rounded hover:bg-gray-100 transition duration-200 ${
                                isActive ? 'text-blue-600 font-semibold' : ''
                            }`}
                        >
                            {item.icon && item.icon}
                            {item.label}
                        </Link>
                    );
                })}
            </div>
        </div>
    );
}

export default Sidebar;
