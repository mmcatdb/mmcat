import { api } from '@/api';
import { File } from '@/types/file';
import { useLoaderData, type Params } from 'react-router-dom';

export function FileDetailPage() {
    const { file } = useLoaderData() as FileDetailLoaderData;
    return (
        // TODO
        <div>
            <div>File Detail Page</div>
            <h2>{file.label}</h2>
            <p>{file.description}</p>
            <p>File Type: {file.fileType}</p>
            <p>Created At: {file.createdAt.toISOString()}</p>
        </div>
    );
}

export type FileDetailLoaderData = {
    file: File;
};

FileDetailPage.loader = async ({ params: { categoryId, fileId } }: { params: Params<'categoryId' | 'fileId'> }): Promise<FileDetailLoaderData> => {
    if (!categoryId)
        throw new Error('Category ID is required');
    if (!fileId)
        throw new Error('File ID is required');

    const response = await api.files.getFile({ id: fileId });
    if (!response.status)
        throw new Error('Failed to load file');

    return {
        file: File.fromResponse(response.data),
    };
};

