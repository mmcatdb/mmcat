import axios from 'axios';
import type { AxiosResponse } from 'axios/index';
import qs from 'qs';
import type { Result } from '@/types/api/result';
import type { PullResult, PushData } from '@/types/api/routes';

const BACKEND_API_URL = import.meta.env.VITE_BACKEND_API_URL;

const instance = axios.create({
    baseURL: BACKEND_API_URL,
    withCredentials: true,
    // timeout: false,
    params: {} // do not remove this, its added to add params later in the config
});

// Add a request interceptor
instance.interceptors.request.use(
    config => {
        return config;
    },
    error => {
        // Do something with request error
        console.log('Request error:', error);
        return Promise.reject(error);
    }
);

function generateErrorObject(error: any) {
    if (!error.response)
        return error;

    // Usually error.response.data = { message: string, code: number }
    const output = typeof(error.response.data) === 'object' && error.response.data ? error.response.data : { nonObjectData: error.response.data };
    output.code = error.response.status;

    return output;
}

instance.interceptors.response.use(
    response => {
        console.log('Response:', response);
        return response;
    },
    error => {
        console.log('Response error:', error);
        console.log('Error config:', error.config);

        if (error.response)
            console.log('Error response:', error.response);

        if (error.request)
            console.log('Error request:', error.request);

        return Promise.reject(generateErrorObject(error));
    }
);

function promiseToResponse<T>(promise: Promise<AxiosResponse<T>>): PullResult<T> {
    return promise
        .then(response => {
            return ({
                status: true,
                data: response.data
            } as Result<T>);
        }).catch(error => {
            return ({
                status: false,
                error: error
            } as Result<T>);
        });
}

function GET<T>(action: string, params = {}): PullResult<T> {
    let url = `${BACKEND_API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.get(url, {
        params,
        paramsSerializer: function (params) {
            return qs.stringify(params, { arrayFormat: 'repeat' });
        }
    }));
}

function POST<T, D extends PushData = void>(action: string, data?: D, params = {}): PullResult<T> {
    let url = `${BACKEND_API_URL}`;
    url += action;

    return promiseToResponse<T>(instance.post(url, data, { params }));
}

function PUT<T, D extends PushData = void>(action: string, data?: D, params = {}): PullResult<T> {
    let url = `${BACKEND_API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.put(url, data, { params }));
}

function DELETE<T>(action: string, params = {}): PullResult<T> {
    let url = `${BACKEND_API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.delete(url, { params }));
}

const rawAPI = {
    GET,
    POST,
    PUT,
    DELETE
};

export default rawAPI;
