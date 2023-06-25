import type { LocationQueryValue, NavigationGuardWithThis } from 'vue-router';

export function toQueryScalar(value: LocationQueryValue | LocationQueryValue[]): LocationQueryValue {
    return typeof value === 'object' ? null : value;
}

export function createQueryParameterKeepingNavigationGuard(parameterName: string): NavigationGuardWithThis<undefined> {
    return (to, from) => {
        const parameterInFrom = toQueryScalar(from.query[parameterName]);
        const parameterInTo = toQueryScalar(to.query[parameterName]);
        if (!parameterInFrom || parameterInTo)
            return true;

        to.query[parameterName] = parameterInFrom;
        return to;
    };
}
