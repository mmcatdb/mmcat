import { Link, useLocation } from 'react-router-dom';

const sidebarItems = [
    {
        label: 'Home',
        route: '/',
    },
    {
        label: 'About',
        route: '/about',
    },
    {
        label: 'Datasources',
        route: '/datasources',
    },
    {
        label: 'Adminer',
        route: '/adminer',
    },
];

function Sidebar() {
    const location = useLocation();

    return (
        <div className='w-64 h-screen p-6 flex flex-col border-r border-grey-300'>
            <h1 className='text-2xl font-semibold mb-10'>New Sidebar</h1>

            {/* Container for the list of sidebar links */}
            <div className='flex flex-col'>
                {sidebarItems.map((item) => {
                    const isActive = item.route === location.pathname;

                    return (
                        <Link
                            key={item.route}
                            to={item.route}
                            className={`block px-4 py-2 rounded hover:bg-gray-100 transition duration-200 ${
                                isActive ? 'text-blue-600 font-semibold' : ''
                            }`}
                        >
                            {item.label}
                        </Link>
                    );
                })}
            </div>
        </div>
    );
}

export default Sidebar;
