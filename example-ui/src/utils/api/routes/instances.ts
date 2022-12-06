import type { Empty, StringLike } from "@/types/api/routes";
import { GET } from "../routeFunctions";
import type { InstanceObjectFromServer } from "@/types/instance";

const instances = {
    getAllInstances: GET<Empty, string[]>(
        () => `/instances`
    ),
    getInstanceObject: GET<{ categoryId: StringLike, objectKey: StringLike }, InstanceObjectFromServer>(
        u => `/instances/${u.categoryId}/objects/${u.objectKey}`
    )
};

export default instances;
