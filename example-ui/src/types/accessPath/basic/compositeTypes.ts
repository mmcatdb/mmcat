import type { ChildPropertyJSON } from "../JSONTypes";
import { ComplexProperty } from "./ComplexProperty";
import type { RootProperty } from "./RootProperty";
import { SimpleProperty } from "./SimpleProperty";

export type ParentProperty = RootProperty | ComplexProperty;

export type ChildProperty = ComplexProperty | SimpleProperty;

export function subpathFromJSON(jsonObject: ChildPropertyJSON, parent: ParentProperty): ChildProperty {
    return 'subpaths' in jsonObject
        ? ComplexProperty.fromJSON(jsonObject, parent)
        : SimpleProperty.fromJSON(jsonObject, parent);
}
