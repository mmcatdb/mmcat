import type { StringLike } from "@/types/api/routes";
import { GET } from "../routeFunctions";
import type { SchemaMorphismFromServer } from "@/types/schema";

const morphisms = {
    getMorphism: GET<{ id: StringLike }, SchemaMorphismFromServer[]>(
        u => `/schema-morphisms/${u.id}`
    )
};

export default morphisms;
