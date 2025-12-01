import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, DELETE } from '../routeFunctions';
import type { ActionResponse, ActionInit, ActionInfo } from '@/types/job';

export const actionsApi = {
    getAllActionsInCategory: GET<{ categoryId: StringLike }, ActionInfo[]>(
        u => `/schema-categories/${u.categoryId}/actions`,
    ),
    getAction: GET<{ id: StringLike }, ActionResponse>(
        u => `/actions/${u.id}`,
    ),
    createAction: POST<Empty, ActionResponse, ActionInit>(
        () => `/actions`,
    ),
    deleteAction: DELETE<{ id: StringLike }, void>(
        u => `/actions/${u.id}`,
    ),
};
