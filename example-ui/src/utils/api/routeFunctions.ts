import type { PullRoute, PushData, PushRoute, QueryParams, Url, UrlParams } from "@/types/api/routes";
import rawAPI from "./rawAPI";

/*
export function GET<U extends UrlParams, T>(url: Url<U>): PullRoute<U, T> {
    return (urlParams: U) => rawAPI.GET<T>(url(urlParams));
}
*/

/*
export function GET<U extends UrlParams, T, Q extends QueryParams = void>(url: Url<U>): PullRoute<U, T, Q> {
    return (urlParams: U, queryParams: Q) => rawAPI.GET<T>(url(urlParams), queryParams);
}
*/

export function GET<U extends UrlParams, T, Q extends QueryParams = void>(url: Url<U>): PullRoute<U, T, Q> {
    return (urlParams: U, queryParams: Q | undefined) => rawAPI.GET<T>(url(urlParams), queryParams || {});
}

export function POST<U extends UrlParams, T, D extends PushData = void>(url: Url<U>): PushRoute<U, T, D> {
    return (urlParams: U, data: D) => rawAPI.POST<T, D>(url(urlParams), data);
}

export function PUT<U extends UrlParams, T, D extends PushData = void>(url: Url<U>): PushRoute<U, T, D> {
    return (urlParams: U, data: D) => rawAPI.PUT<T, D>(url(urlParams), data);
}

export function DELETE<U extends UrlParams, T>(url: Url<U>): PullRoute<U, T, void> {
    return (urlParams: U) => rawAPI.DELETE<T>(url(urlParams));
}
