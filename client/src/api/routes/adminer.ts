import { GET } from '../routeFunctions';
import type { StringLike, QueryParams } from '@/types/api/routes';
import type { DataResponse } from '@/types/adminer/DataResponse';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';

const adminer = {
    getKindNames: GET<{ datasourceId: StringLike }, string[]>(
        u => `/adminer/${u.datasourceId}`,
    ),
    getRecords: GET<{ datasourceId: StringLike }, DataResponse, QueryParams>(
        u => `/adminer/${u.datasourceId}/kind`,
    ),
    getReferences: GET<{ datasourceId: StringLike }, AdminerReferences, QueryParams>(
        u => `/adminer/${u.datasourceId}/references`,
    ),
    getQueryResult: GET<{ datasourceId: StringLike}, DataResponse, QueryParams>(
        u => `/adminer/${u.datasourceId}/query`,
    ),
};

export default adminer;
