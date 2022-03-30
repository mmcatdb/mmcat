//import { ComplexProperty, type ComplexPropertyJSON } from "./ComplexProperty";
import { ComplexProperty, type ComplexPropertyJSON } from "./ComplexProperty";
import type { ChildProperty, ParentProperty } from "./compositeTypes";
//import { SimpleProperty, type SimplePropertyJSON } from "./SimpleProperty";
import { SimpleProperty, type SimplePropertyJSON } from "./SimpleProperty";

export type AccessPathJSON = ComplexPropertyJSON | SimplePropertyJSON;

//export type AccessPath = SimpleProperty | ComplexProperty;
/*
export function accessPathFromJSON(jsonObject: AccessPathJSON): AccessPath {
    return jsonObject._class === 'SimpleProperty' ? SimpleProperty.fromJSON(jsonObject) : ComplexProperty.fromJSON(jsonObject);
}
*/

export function subpathFromJSON(jsonObject: AccessPathJSON, parent: ParentProperty): ChildProperty {
    return jsonObject._class === 'SimpleProperty' ? SimpleProperty.fromJSON(jsonObject, parent) : ComplexProperty.fromJSON(jsonObject, parent);
}


