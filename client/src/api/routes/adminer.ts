import type { StringLike, QueryParams } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { TableResponse, DataResponse } from '@/types/adminer/BackendResponse';

const adminer = {
    getKindNames: GET<{ datasourceId: StringLike }, TableResponse>(
        (u) => `/adminer/${u.datasourceId}`,
    ),
    getKind: GET<{ datasourceId: StringLike, kindId: StringLike }, DataResponse, QueryParams>(
        u => `/adminer/${u.datasourceId}/${u.kindId}`,
    ),
};

export default adminer;
