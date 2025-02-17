import { useParams } from 'react-router-dom';

export function QueryingPage() {
    const { categoryId } = useParams<{ categoryId: string }>();

    return (
        <div>
            <h1 className='text-2xl font-bold mb-4'>Querying for Project {categoryId}</h1>
            <p>This page is not implemented in this prototype. The functionality, existing as one of the frameworks (MM-quecat), is planned to be integrated in the future. As this is a prototype the focus is on core features, with querying to be incorporated later. However this page has been added to outline where querying will be placed.</p>
        </div>
    );
}
