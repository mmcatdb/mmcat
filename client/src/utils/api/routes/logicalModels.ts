import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { LogicalModelFromServer, LogicalModelInfoFromServer, LogicalModelInit } from '@/types/logicalModel';
import type { DatabaseInfoFromServer } from '@/types/database';

type LogicalModelDatabaseInfoFromServer = {
    logicalModel: LogicalModelInfoFromServer;
    database: DatabaseInfoFromServer;
};

const logicalModels = {
    getLogicalModel: GET<{ id: StringLike }, LogicalModelFromServer>(
        u => `/logical-models/${u.id}`,
    ),
    getAllLogicalModelsInCategory: GET<{ categoryId: StringLike }, LogicalModelFromServer[]>(
        u => `/schema-categories/${u.categoryId}/logical-models`,
    ),
    createNewLogicalModel: POST<Empty, LogicalModelInfoFromServer, LogicalModelInit>(
        () => `/logical-models`,
    ),
    getAllLogicalModelDatabaseInfosInCategory: GET<{ categoryId: StringLike }, LogicalModelDatabaseInfoFromServer[]>(
        u => `/schema-categories/${u.categoryId}/logical-model-infos`,
    ),
};

export default logicalModels;
