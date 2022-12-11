
import type { PullResult } from "@/types/api/routes";
import qs from "qs";
import { createAxiosInstance, promiseToResponse } from "./common";

const DATASPECER_API_URL = import.meta.env.VITE_DATASPECER_API_URL;

const instance = createAxiosInstance();

function GET<T>(url: string, params = {}): PullResult<T> {
    return promiseToResponse<T>(instance.get(url, {
        params,
        paramsSerializer: function (params) {
            return qs.stringify(params, { arrayFormat: 'repeat' });
        }
    }));
}

type DataSpecification = {
    iri: string;
    pimStores?: {
        url: string;
    }[];
}

async function getStoreForIri(iri: string): PullResult<unknown> {
    const dataSpecificationsResult = await GET<DataSpecification[]>(DATASPECER_API_URL + '/data-specification');
    if (!dataSpecificationsResult.status)
        return dataSpecificationsResult;

    const dataSpecification = dataSpecificationsResult.data.find(s => s.iri === iri);
    if (!dataSpecification)
        return { status: false, error: 'Data specification was not found.' };

    if (!dataSpecification.pimStores || dataSpecification.pimStores.length === 0)
        return { status: false, error: 'No store url was found.' };

    const store = dataSpecification.pimStores[0];

    return GET<unknown>(store.url);
}

const dataspecerAPI = {
    getStoreForIri
};

export default dataspecerAPI;
