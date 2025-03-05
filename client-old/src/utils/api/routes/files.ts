import type { StringLike } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { FileFromServer } from '@/types/file';

const files = {
    getAllFilesInCategory: GET<{ categoryId: StringLike }, FileFromServer[]>(
        u => `/schema-categories/${u.categoryId}/files`,
    ),
    downloadFile: GET<{ id: StringLike }, Response>(
        u => `/files/${u.id}`, 
    ),
};

export default files;