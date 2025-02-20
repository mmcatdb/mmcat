import type { GraphComplexProperty } from './GraphComplexProperty';
import type { GraphRootProperty } from './GraphRootProperty';
import type { GraphSimpleProperty } from './GraphSimpleProperty';

/** @deprecated */
export type GraphParentProperty = GraphRootProperty | GraphComplexProperty;

/** @deprecated */
export type GraphChildProperty = GraphComplexProperty | GraphSimpleProperty;
