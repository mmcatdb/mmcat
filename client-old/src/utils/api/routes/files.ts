import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { FileFromServer } from '@/types/file';

const files = {
    getAllFilesInCategory: GET<{ categoryId: StringLike }, FileFromServer[]>(
        u => `/schema-categories/${u.categoryId}/files`,
    ),
    downloadFile: GET<{ id: StringLike }, Response>(
        u => `/files/${u.id}/download`, 
    ),
    executeDML: POST<{ id: StringLike }, void>(
        u => `/files/${u.id}/execute`,
    ),
};

export default files;