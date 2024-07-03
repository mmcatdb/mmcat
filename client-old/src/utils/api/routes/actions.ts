import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, DELETE } from '../routeFunctions';
import type { ActionFromServer, ActionInit } from '@/types/action';

const actions = {
    getAllActionsInCategory: GET<{ categoryId: StringLike }, ActionFromServer[]>(
        u => `/schema-categories/${u.categoryId}/actions`,
    ),
    getAction: GET<{ id: StringLike }, ActionFromServer>(
        u => `/actions/${u.id}`,
    ),
    createAction: POST<Empty, ActionFromServer, ActionInit>(
        () => `/actions`,
    ),
    deleteAction: DELETE<{ id: StringLike }, void>(
        u => `/actions/${u.id}`,
    ),
};

export default actions;
