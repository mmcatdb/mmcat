import { Link } from 'react-router-dom';
import { Navbar } from '@nextui-org/react';

const Sidebar: React.FC = () => {
    return (
        <div className='w-64 h-screen bg-gray-800 text-white p-6 flex flex-col'>
            <h1 className='text-2xl font-semibold mb-10'>New Sidebar</h1>

            <Navbar className='flex flex-col space-y-6'>
                <Link to='/' className='hover:text-gray-400 transition duration-200'>
                    Home
                </Link>
                <Link to='/about' className='hover:text-gray-400 transition duration-200'>
                    About
                </Link>
                <Link to='/data-sources' className='hover:text-gray-400 transition duration-200'>
                    Data Sources
                </Link>
            </Navbar>
        </div>
    );
};

export default Sidebar;
