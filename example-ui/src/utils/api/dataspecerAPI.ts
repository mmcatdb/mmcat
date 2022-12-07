import type { PullResult } from "@/types/api/routes";
import qs from "qs";
import { createAxiosInstance, promiseToResponse } from "./common";

const DATASPECER_API_URL = import.meta.env.VITE_DATASPECER_API_URL;

const instance = createAxiosInstance(DATASPECER_API_URL);

function GET<T>(action: string, params = {}): PullResult<T> {
    return promiseToResponse<T>(instance.get(action, {
        params,
        paramsSerializer: function (params) {
            return qs.stringify(params, { arrayFormat: 'repeat' });
        }
    }));
}

const dataspecerAPI = {
    GET
};

export default dataspecerAPI;
