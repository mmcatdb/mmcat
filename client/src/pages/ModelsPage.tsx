import { Link, Outlet, useParams } from 'react-router-dom';

export const ModelsPage = () => {
    const { categoryId } = useParams<'categoryId'>();

    return (
        <div>
            <h2>Models for Project {categoryId}</h2>
            <p>Template page</p>
            <Link to='mappings/1'>
            Go here
            </Link>
            <Outlet />
        </div>
    );
};
