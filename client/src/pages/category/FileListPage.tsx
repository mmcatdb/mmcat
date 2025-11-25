import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { routes } from '@/routes/routes';
import { File } from '@/types/file';
import { Link, useLoaderData, type Params } from 'react-router-dom';

export function FileListPage() {
    const { category } = useCategoryInfo();
    const { files } = useLoaderData() as FileListLoaderData;

    return (
        // TODO
        <div>
            <div>File List Page</div>
            {files.map(file => (
                <div key={file.id}>
                    <Link
                        to={routes.category.files.detail.resolve({ categoryId: category.id, fileId: file.id })}
                        className='text-primary-500 hover:underline'
                    >
                        {file.label}
                    </Link>
                    <p>{file.description}</p>
                </div>
            ))}
        </div>
    );
}

type FileListLoaderData = {
    files: File[];
};

FileListPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }): Promise<FileListLoaderData> => {
    if (!categoryId)
        throw new Error('Category ID is required');

    const response = await api.files.getAllFilesInCategory({ categoryId });
    if (!response.status)
        throw new Error('Failed to load files');

    return {
        files: response.data.map(File.fromResponse),
    };
};
