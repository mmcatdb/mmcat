import type { Empty, StringLike } from "@/types/api/routes";
import { GET, POST, PUT } from "../routeFunctions";
import type { PositionUpdate, SchemaCategoryFromServer, SchemaCategoryInfoFromServer, SchemaCategoryInit, SchemaCategoryUpdate } from "@/types/schema";

const schemas = {
    getAllCategoryInfos: GET<Empty, SchemaCategoryInfoFromServer[]>(
        () => `/schema-categories`
    ),
    createNewSchema: POST<Empty, SchemaCategoryInfoFromServer, SchemaCategoryInit>(
        () => `/schema-categories`
    ),
    getCategoryInfo: GET<{ id: StringLike }, SchemaCategoryInfoFromServer>(
        u => `/schema-categories/${u.id}/info`
    ),
    getCategoryWrapper: GET<{ id: StringLike }, SchemaCategoryFromServer>(
        u => `/schema-categories/${u.id}`
    ),
    updateCategoryWrapper: PUT<{ id: StringLike }, SchemaCategoryFromServer, SchemaCategoryUpdate>(
        u => `/schema-categories/${u.id}`
    ),
    updateCategoryPositions: PUT<{ id: StringLike }, boolean, PositionUpdate[]>(
        u => `/schema-categories/${u.id}/positions`
    )
};

export default schemas;
