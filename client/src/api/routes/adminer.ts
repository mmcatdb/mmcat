import { GET } from '../routeFunctions';
import type { StringLike, QueryParams } from '@/types/api/routes';
import type { DataResponse, KindNameResponse } from '@/types/adminer/DataResponse';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';

const adminer = {
    getKindNames: GET<{ datasourceId: StringLike }, KindNameResponse>(
        u => `/adminer/${u.datasourceId}`,
    ),
    getKind: GET<{ datasourceId: StringLike, kindName: StringLike }, DataResponse, QueryParams>(
        u => `/adminer/${u.datasourceId}/${u.kindName}`,
    ),
    getReferences: GET<{ datasourceId: StringLike, kindName: StringLike }, AdminerReferences>(
        u => `/adminer/${u.datasourceId}/${u.kindName}/references`,
    ),
};

export default adminer;
