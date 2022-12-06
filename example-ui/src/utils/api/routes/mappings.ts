import type { Empty, StringLike } from "@/types/api/routes";
import { GET, POST } from "../routeFunctions";
import type { MappingFromServer, MappingInfoFromServer, MappingInit } from "@/types/mapping";

const mappings = {
    getMapping: GET<{ id: StringLike }, MappingFromServer>(
        u => `/mappings/${u.id}`
    ),
    getAllMappingsInLogicalModel: GET<{ logicalModelId: StringLike }, MappingFromServer[]>(
        u => `/logical-models/${u.logicalModelId}/mappings`
    ),
    createNewMapping: POST<Empty, MappingInfoFromServer, MappingInit>(
        () => `/mappings`
    ),
    getAllMappingsInCategory: GET<{ categoryId: StringLike }, MappingFromServer[]>(
        u => `/schema-categories/${u.categoryId}/mappings`
    )
};

export default mappings;
