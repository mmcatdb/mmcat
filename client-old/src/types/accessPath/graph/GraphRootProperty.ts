import type { Node } from '@/types/categoryGraph';
import type { StaticName } from '@/types/identifiers';
import type { RootPropertyFromServer } from '../serverTypes';
import type { GraphChildProperty, GraphParentProperty } from './compositeTypes';
import { SequenceSignature } from './SequenceSignature';
import { GraphComplexProperty } from './GraphComplexProperty';

export class GraphRootProperty {
    name: StaticName;
    _subpaths: GraphChildProperty[];
    _signature: SequenceSignature;

    constructor(name: StaticName, rootNode: Node, subpaths: GraphChildProperty[] = []) {
        this.name = name;
        this._subpaths = [ ...subpaths ];
        this._signature = SequenceSignature.empty(rootNode);
    }

    containsNode(node: Node): boolean {
        if (this.node.equals(node)) return true;      

        return this._subpaths.some(subpath => this.searchSubpathsForNode(subpath, node));
    }

    searchSubpathsForNode(property: GraphParentProperty, node: Node): boolean {
        if (property.node.equals(node)) return true;        

        if (property instanceof GraphComplexProperty) 
            return property.subpaths.some(subpath => this.searchSubpathsForNode(subpath, node));
        
        return false;
    }

    highlightPath() {
        this.node.highlight();
        this.highlightSubpaths(this._subpaths);
    }

    private highlightSubpaths(subpaths: GraphChildProperty[]) {
        subpaths.forEach(subpath => {
            subpath.node.highlight();
            if (subpath instanceof GraphComplexProperty) 
                this.highlightSubpaths(subpath.subpaths);
        });
    }

    unhighlightPath() {
        this.node.unhighlight();
        this.unhighlightSubpaths(this._subpaths);
    }

    private unhighlightSubpaths(subpaths: GraphChildProperty[]) {
        subpaths.forEach(subpath => {
            subpath.node.unhighlight();
            if (subpath instanceof GraphComplexProperty) 
                this.unhighlightSubpaths(subpath.subpaths);
        });
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

    removeSubpathForNode(node: Node): void {
        if (this.node.equals(node)) {
            console.warn('Cannot remove the root node.');
            return;
        }
    
        const subpathToRemove = this._subpaths.find(subpath => subpath.node.equals(node));
        if (subpathToRemove) {
            this.removeSubpath(subpathToRemove);
            return;
        }
    
        for (const subpath of this._subpaths) {
            if (subpath instanceof GraphComplexProperty) 
                subpath.removeSubpathForNode(node);            
        }
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
