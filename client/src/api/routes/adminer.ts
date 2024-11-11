import type { StringLike, QueryParams } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { BackendTableResponse } from '@/types/adminer/BackendResponse';

const adminer = {
    getKindNames: GET<{ datasourceId: StringLike }, BackendTableResponse>(
        (u) => `/adminer/${u.datasourceId}`,
    ),
    getKind: GET<{ datasourceId: StringLike, kindId: StringLike }, BackendTableResponse, QueryParams>(
        u => `/adminer/${u.datasourceId}/${u.kindId}`,
    ),
};

export default adminer;
