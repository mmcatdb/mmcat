import type { ChildPropertyFromServer } from '../serverTypes';
import { ComplexProperty } from './ComplexProperty';
import type { RootProperty } from './RootProperty';
import { SimpleProperty } from './SimpleProperty';

export type ParentProperty = RootProperty | ComplexProperty;

export type ChildProperty = ComplexProperty | SimpleProperty;

export function subpathFromFromServer(input: ChildPropertyFromServer, parent: ParentProperty): ChildProperty {
    return 'subpaths' in input
        ? ComplexProperty.fromServer(input, parent)
        : SimpleProperty.fromServer(input, parent);
}
