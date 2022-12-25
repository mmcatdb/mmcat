import type { ChildPropertyJSON } from "../JSONTypes";
import { ComplexProperty } from "./ComplexProperty";
import type { RootProperty } from "./RootProperty";
import { SimpleProperty } from "./SimpleProperty";

export type ParentProperty = RootProperty | ComplexProperty;

export type ChildProperty = ComplexProperty | SimpleProperty;

export function subpathFromJSON(jsonObject: ChildPropertyJSON, parent: ParentProperty): ChildProperty {
    return jsonObject._class === 'SimpleProperty' ? SimpleProperty.fromJSON(jsonObject, parent) : ComplexProperty.fromJSON(jsonObject, parent);
}
