import type { Empty, StringLike } from "@/types/api/routes";
import { GET, POST } from "../routeFunctions";
import type { LogicalModelFromServer, LogicalModelInfoFromServer, LogicalModelInit } from "@/types/logicalModel";

const logicalModels = {
    getLogicalModel: GET<{ id: StringLike }, LogicalModelFromServer>(
        u => `/logical-models/${u.id}`
    ),
    getAllLogicalModelsInCategory: GET<{ categoryId: StringLike }, LogicalModelFromServer[]>(
        u => `/schema-categories/${u.categoryId}/logical-models`
    ),
    createNewLogicalModel: POST<Empty, LogicalModelInfoFromServer, LogicalModelInit>(
        () => `/logical-models`
    ),
    getAllLogicalModelInfosInCategory: GET<{ categoryId: StringLike }, LogicalModelInfoFromServer[]>(
        u => `/schema-categories/${u.categoryId}/logical-model-infos`
    )
};

export default logicalModels;
