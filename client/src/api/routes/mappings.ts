import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { MappingResponse, MappingInit } from '@/types/mapping';
import { type Id } from '@/types/id';

const mappings = {
    getMapping: GET<{ id: StringLike }, MappingResponse>(
        u => `/mappings/${u.id}`,
    ),
    getAllMappings: GET<Empty, MappingResponse[]>(
        () => `/mappings/all`,
    ),
    getAllMappingsInCategory: GET<Empty, MappingResponse[], { categoryId: Id, datasourceId?: Id }>(
        () => `/mappings`,
    ),
    createMapping: POST<Empty, MappingResponse, MappingInit>(
        () => `/mappings`,
    ),
};

export default mappings;
