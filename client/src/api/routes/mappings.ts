import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { MappingFromServer, MappingInit } from '@/types/mapping';
import { type Id } from '@/types/id';

const mappings = {
    getMapping: GET<{ id: StringLike }, MappingFromServer>(
        u => `/mappings/${u.id}`,
    ),
    getAllMappings: GET<Empty, MappingFromServer[]>(
        () => `/mappings/all`,
    ),
    getAllMappingsInCategory: GET<Empty, MappingFromServer[], { categoryId: Id, datasourceId?: Id }>(
        () => `/mappings`,
    ),
    createMapping: POST<Empty, MappingFromServer, MappingInit>(
        () => `/mappings`,
    ),
};

export default mappings;
