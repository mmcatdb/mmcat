import { GET } from '../routeFunctions';
import type { StringLike, QueryParams } from '@/types/api/routes';
import type { DataResponse, KindNameResponse } from '@/types/adminer/DataResponse';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';

const adminer = {
    getKindNames: GET<{ datasourceId: StringLike }, KindNameResponse>(
        u => `/adminer/${u.datasourceId}`,
    ),
    // FIXME Toto je spíš drobnost, nicméně kindName by se neměl předávat v path části url, protože může teoreticky obsahovat nevalidní znaky či věci typu /, #, ?, atd. Neříkám, že to je časté, ale může to nastat. Takže by se to spíš mělo předávat v search části.
    getKind: GET<{ datasourceId: StringLike, kindName: StringLike }, DataResponse, QueryParams>(
        u => `/adminer/${u.datasourceId}/${u.kindName}`,
    ),
    getReferences: GET<{ datasourceId: StringLike, kindName: StringLike }, AdminerReferences>(
        u => `/adminer/${u.datasourceId}/${u.kindName}/references`,
    ),
    getQueryResult: GET<{ datasourceId: StringLike}, DataResponse, QueryParams>(
        u => `/adminer/${u.datasourceId}/query`,
    ),
};

export default adminer;
