import type { GraphComplexProperty } from './GraphComplexProperty';
import type { GraphRootProperty } from './GraphRootProperty';
import type { GraphSimpleProperty } from './GraphSimpleProperty';

export type GraphParentProperty = GraphRootProperty | GraphComplexProperty;

export type GraphChildProperty = GraphComplexProperty | GraphSimpleProperty;
