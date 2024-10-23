import { useParams } from 'react-router-dom';

export const ModelsPage = () => {
    const { projectId } = useParams<'projectId'>();

    return (
        <div>
            <h2>Models for Project {projectId}</h2>
            <p>Template page</p>
        </div>
    );
};
