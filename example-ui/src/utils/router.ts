import type { LocationQueryValue } from "vue-router";

export function toQueryScalar(value: LocationQueryValue | LocationQueryValue[]): LocationQueryValue {
    return typeof value === 'object' ? null : value;
}
