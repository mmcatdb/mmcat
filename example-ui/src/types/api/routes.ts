import type { Result } from "./result";

export type StringLike = string | string[] | number;

export type Empty = Record<string, never>;

export type UrlParams = {
    [ key: string ]: StringLike;
} | Empty;

export type UrlFunction<U extends UrlParams> = (urlParams: U) => string

//export type Url<U extends UrlParams> = U extends UrlParams ? UrlFunction<U> : string;
export type Url<U extends UrlParams> = UrlFunction<U>;

export type PullResult<T> = Promise<Result<T>>;

export type PullRoute<U extends UrlParams, T> = (urlParams: U) => PullResult<T>;

export type PushData = Record<string, unknown> | Record<string, unknown>[] | void;

export type PushRoute<U extends UrlParams, T, D extends PushData> = (urlParams: U, data: D) => PullResult<T>;

export type ApiRoute<U extends UrlParams, T, D extends PushData = void> = PullRoute<U, T> | PushRoute<U, T, D>;

/*
export type API = {
    readonly [ key: string ]: ApiPath<UrlParams, unknown, PushData>
}
*/
