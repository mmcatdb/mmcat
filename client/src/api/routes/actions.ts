import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, DELETE } from '../routeFunctions';
import type { ActionResponse, ActionInit } from '@/types/action';

const actions = {
    getAllActionsInCategory: GET<{ categoryId: StringLike }, ActionResponse[]>(
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

export default actions;
