import { useParams } from 'react-router-dom';

export const ModelsPage = () => {
    const { categoryId } = useParams<'categoryId'>();

    return (
        <div>
            <h2>Models for Project {categoryId}</h2>
            <p>Template page</p>
        </div>
    );
};
