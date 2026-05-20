import { type FreeSelection } from './FreeSelection';
import { type SequenceSelection } from './SequenceSelection';

export * from './FreeSelection';
export * from './SequenceSelection';

export type GraphSelection = FreeSelection | SequenceSelection;
