import type { Empty, StringLike } from "@/types/api/routes";
import { GET } from "../routeFunctions";
import type { InstanceMorphismFromServer, InstanceObjectFromServer } from "@/types/instance";

const instances = {
    getAllInstances: GET<Empty, string[]>(
        () => `/instances`
    ),
    getInstanceObject: GET<{ categoryId: StringLike, objectKey: StringLike }, InstanceObjectFromServer>(
        u => `/instances/${u.categoryId}/objects/${u.objectKey}`
    ),
    getInstanceMorphism: GET<{ categoryId: StringLike, signature: StringLike }, InstanceMorphismFromServer>(
        u => `/instances/${u.categoryId}/morphisms/${u.signature}`
    )
};

export default instances;
