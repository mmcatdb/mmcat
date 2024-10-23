import { useParams } from 'react-router-dom';

export const QueryingPage = () => {
    const { projectId } = useParams<'projectId'>();

    return (
        <div>
            <h2>Querying for Project {projectId}</h2>
            <p>Template page</p>
        </div>
    );
};
