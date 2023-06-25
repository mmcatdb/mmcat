import qs from 'qs';
import type { PullResult, PushData } from '@/types/api/routes';
import { createAxiosInstance, promiseToResponse } from './common';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

const instance = createAxiosInstance(BACKEND_API_URL);

function GET<T>(action: string, params = {}): PullResult<T> {
    return promiseToResponse<T>(instance.get(action, {
        params,
        paramsSerializer: (p) => qs.stringify(p, { arrayFormat: 'repeat' }),
    }));
}

function POST<T, D extends PushData = void>(action: string, data?: D, params = {}): PullResult<T> {
    return promiseToResponse<T>(instance.post(action, data, { params }));
}

function PUT<T, D extends PushData = void>(action: string, data?: D, params = {}): PullResult<T> {
    return promiseToResponse<T>(instance.put(action, data, { params }));
}

function DELETE<T>(action: string, params = {}): PullResult<T> {
    return promiseToResponse<T>(instance.delete(action, { params }));
}

const rawAPI = {
    GET,
    POST,
    PUT,
    DELETE,
};

export default rawAPI;
