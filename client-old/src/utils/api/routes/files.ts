import type { StringLike } from '@/types/api/routes';
import { GET, POST, PUT } from '../routeFunctions';
import type { FileEdit, FileFromServer } from '@/types/file';

const files = {
    getAllFilesInCategory: GET<{ categoryId: StringLike }, FileFromServer[]>(
        u => `/schema-categories/${u.categoryId}/files`,
    ),
    downloadFile: GET<{ id: StringLike }, Blob>(
        u => `/files/${u.id}/download`,
    ),
    executeDML: POST<{ id: StringLike }, FileFromServer, { mode: string, newDBName?: string }>(
        u => `/files/${u.id}/execute`,
    ),
    updateFile: PUT<{ id: StringLike }, FileFromServer, FileEdit>(
        u => `/files/${u.id}/update`,
    ),
    previewFile: GET<{ id: StringLike }, String>(
        u => `/files/${u.id}/preview`,
    ),
};

export default files;
