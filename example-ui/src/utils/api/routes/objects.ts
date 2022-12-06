import type { StringLike } from "@/types/api/routes";
import { GET } from "../routeFunctions";
import type { SchemaObjectFromServer } from "@/types/schema";

const objects = {
    getObject: GET<{ id: StringLike }, SchemaObjectFromServer[]>(
        u => `/schema-objects/${u.id}`
    )
};

export default objects;
