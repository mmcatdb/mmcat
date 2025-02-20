import type { Node } from '@/types/categoryGraph';
import type { StaticName } from '@/types/identifiers';
import type { RootPropertyFromServer } from '../serverTypes';
import type { GraphChildProperty } from './compositeTypes';
import { SequenceSignature } from './SequenceSignature';

/** @deprecated */
export class GraphRootProperty {
    name: StaticName;
    _subpaths: GraphChildProperty[];
    _signature: SequenceSignature;

    constructor(name: StaticName, rootNode: Node, subpaths: GraphChildProperty[] = []) {
        this.name = name;
        this._subpaths = [ ...subpaths ];
        this._signature = SequenceSignature.empty(rootNode);
    }

    update(newName: StaticName): void {
        if (!this.name.equals(newName))
            this.name = newName;
    }

    updateOrAddSubpath(newSubpath: GraphChildProperty, oldSubpath?: GraphChildProperty): void {
        newSubpath.parent = this;
        const index = oldSubpath ? this._subpaths.findIndex(subpath => subpath.signature.equals(oldSubpath.signature)) : -1;
        if (index === -1)
            this._subpaths.push(newSubpath);
        else
            this._subpaths[index] = newSubpath;
    }

    removeSubpath(oldSubpath: GraphChildProperty): void {
        this._subpaths = this._subpaths.filter(subpath => !subpath.signature.equals(oldSubpath.signature));
    }

    get isAuxiliary(): boolean {
        return true;
    }

    get signature(): SequenceSignature {
        return this._signature;
    }

    get node(): Node {
        return this._signature.sequence.lastNode;
    }

    get subpaths(): GraphChildProperty[] {
        return this._subpaths;
    }

    toServer(): RootPropertyFromServer {
        return {
            name: this.name.toServer(),
            signature: this._signature.toSignature().toServer(),
            subpaths: this._subpaths.map(subpath => subpath.toServer()),
        };
    }
}
