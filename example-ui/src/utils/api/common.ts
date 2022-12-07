import axios from 'axios';
import type { AxiosResponse } from 'axios/index';
import type { Result } from '@/types/api/result';
import type { PullResult } from '@/types/api/routes';

export function createAxiosInstance(baseURL: string) {
    const instance = axios.create({
        baseURL,
        //withCredentials: true,
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

    return instance;
}

export function promiseToResponse<T>(promise: Promise<AxiosResponse<T>>): PullResult<T> {
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
