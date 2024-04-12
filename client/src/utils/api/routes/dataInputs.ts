import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, PUT, DELETE } from '../routeFunctions';
import type { DataInput, DataInputInit, DataInputUpdate, DataInputInfoFromServer, DataInputWithConfigurationFromServer } from '@/types/dataInput';
import type { Id } from '@/types/id';

const dataInputs = {
    getAllDataInputInfos: GET<Empty, DataInputWithConfigurationFromServer[]>(
        () => `/data-input-infos`,
    ),
    getAllDataInputs: GET<Empty, DataInput[], { categoryId: Id }>(
        () => `/data-inputs`,
    ),
    getDataInput: GET<{ id: StringLike }, DataInput>(
        u => `/data-inputs/${u.id}`,
    ),
    createDataInput: POST<Empty, DataInputInfoFromServer, DataInputInit>(
        () => `/data-inputs`,
    ),
    updateDataInput: PUT<{ id: StringLike }, DataInputInfoFromServer, DataInputUpdate>(
        u => `/data-inputs/${u.id}`,
    ),
    deleteDataInput: DELETE<{ id: StringLike }, void>(
        u => `/data-inputs/${u.id}`,
    ),
};

export default dataInputs;
