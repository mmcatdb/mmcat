import { ComplexProperty, type ComplexPropertyJSON } from "./ComplexProperty";
import type { RootProperty } from "./RootProperty";
import { SimpleProperty, type SimplePropertyJSON } from "./SimpleProperty";

export type ParentProperty = RootProperty | ComplexProperty;

export type ChildPropertyJSON = ComplexPropertyJSON | SimplePropertyJSON;

export type ChildProperty = ComplexProperty | SimpleProperty;

export function subpathFromJSON(jsonObject: ChildPropertyJSON, parent: ParentProperty): ChildProperty {
    return jsonObject._class === 'SimpleProperty' ? SimpleProperty.fromJSON(jsonObject, parent) : ComplexProperty.fromJSON(jsonObject, parent);
}
