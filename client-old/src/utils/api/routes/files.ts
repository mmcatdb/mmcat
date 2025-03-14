import type { StringLike } from '@/types/api/routes';
import { GET, POST, PUT } from '../routeFunctions';
import type { FileFromServer } from '@/types/file';

const files = {
    getAllFilesInCategory: GET<{ categoryId: StringLike }, FileFromServer[]>(
        u => `/schema-categories/${u.categoryId}/files`,
    ),
    downloadFile: GET<{ id: StringLike }, Response>(
        u => `/files/${u.id}/download`, 
    ),
    executeDML: POST<{ id: StringLike }, FileFromServer>(
        u => `/files/${u.id}/execute`,
    ),
    updateFile: PUT<{ id: StringLike }, FileFromServer, { value: string, isLabel: boolean }>(
        u => `/files/${u.id}/update`,
    ),
};

export default files;