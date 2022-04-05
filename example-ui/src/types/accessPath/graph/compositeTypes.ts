import type { ComplexProperty, ComplexPropertyJSON } from "./ComplexProperty";
import type { RootProperty } from "./RootProperty";
import type { SimpleProperty, SimplePropertyJSON } from "./SimpleProperty";

export type ParentProperty = RootProperty | ComplexProperty;

export type ChildPropertyJSON = ComplexPropertyJSON | SimplePropertyJSON;

export type ChildProperty = ComplexProperty | SimpleProperty;
