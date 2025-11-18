import { type GraphSelection } from '@/components/graph/selection';
import { type PathSelection } from './PathSelection';

export * from '@/components/graph/selection';
export * from './PathGraph';
export * from './PathSelection';

export type CategoryGraphSelection = GraphSelection | PathSelection;
