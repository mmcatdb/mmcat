import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { MappingFromServer, MappingInit } from '@/types/mapping';

const mappings = {
    getMapping: GET<{ id: StringLike }, MappingFromServer>(
        u => `/mappings/${u.id}`,
    ),
    getAllMappingsInDatasource: GET<{ datasourceId: StringLike }, MappingFromServer[]>(
        u => `/datasources/${u.datasourceId}/mappings`,
    ),
    createMapping: POST<Empty, MappingFromServer, MappingInit>(
        () => `/mappings`,
    ),
};

export default mappings;
