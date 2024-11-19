import { useParams } from 'react-router-dom';

export const QueryingPage = () => {
    const { categoryId } = useParams<'categoryId'>();

    return (
        <div>
            <h2>Querying for Project {categoryId}</h2>
            <p>Template page</p>
        </div>
    );
};