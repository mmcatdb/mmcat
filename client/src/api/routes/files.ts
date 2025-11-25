import type { StringLike } from '@/types/api/routes';
import { GET, POST, PUT } from '../routeFunctions';
import type { FileEdit, FileResponse } from '@/types/file';

export const filesApi = {
    getFile: GET<{ id: StringLike }, FileResponse>(
        u => `/files/${u.id}`,
    ),
    getAllFilesInCategory: GET<{ categoryId: StringLike }, FileResponse[]>(
        u => `/schema-categories/${u.categoryId}/files`,
    ),
    downloadFile: GET<{ id: StringLike }, Blob>(
        u => `/files/${u.id}/download`,
    ),
    previewFile: GET<{ id: StringLike }, string>(
        u => `/files/${u.id}/preview`,
    ),
    updateFile: PUT<{ id: StringLike }, FileResponse, FileEdit>(
        u => `/files/${u.id}`,
    ),
    executeDML: POST<{ id: StringLike }, FileResponse, { mode: DMLExecutionMode, newDBName?: string }>(
        u => `/files/${u.id}/execute`,
    ),
};

export enum DMLExecutionMode {
    execute = 'EXECUTE',
    createNewAndExecute = 'CREATE_NEW_AND_EXECUTE',
    deleteAndExecute = 'DELETE_AND_EXECUTE',
}
