import axios from 'axios';
import type { AxiosResponse } from 'axios/index';
//import { API_URL } from '@/config';
import qs from 'qs';
import type { Result } from '@/types/result';

const API_URL = 'http://localhost:27500';

const instance = axios.create({
    baseURL: API_URL,
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

function promiseToResponse<T>(promise: Promise<AxiosResponse<T>>): Promise<Result<T>> {
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

export function GET<T>(action: string, params = {}): Promise<Result<T>> {
    let url = `${API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.get(url, {
        params,
        paramsSerializer: function(params) {
            return qs.stringify(params, { arrayFormat: 'repeat' });
        }
    }));
}

export function POST<T>(action: string, data = {}, params = {}): Promise<Result<T>> {
    let url = `${API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.post(url, data, { params }));
}

export function PUT<T>(action: string, data = {}, params = {}): Promise<Result<T>> {
    let url = `${API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.put(url, data, { params }));
}

export function PATCH<T>(action: string, data = {}, params = {}): Promise<Result<T>> {
    let url = `${API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.patch(url, data, { params }));
}

export function DELETE<T>(action: string, params = {}): Promise<Result<T>> {
    let url = `${API_URL}`;
    url += action;
    return promiseToResponse<T>(instance.delete(url, { params }));
}
