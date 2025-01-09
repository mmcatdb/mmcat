import type { Node } from '@/types/categoryGraph';
import type { StaticName } from '@/types/identifiers';
import type { RootPropertyFromServer } from '../serverTypes';
import { RootProperty, SimpleProperty, ComplexProperty } from '../basic';
import type { GraphChildProperty, GraphParentProperty } from './compositeTypes';
import { SequenceSignature } from './SequenceSignature';
import { GraphComplexProperty } from './GraphComplexProperty';
import { GraphSimpleProperty } from './GraphSimpleProperty';
import type { ChildProperty } from '@/types/accessPath/basic/compositeTypes';

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

    searchSubpathsForNode(property: GraphParentProperty | GraphSimpleProperty, node: Node): boolean {
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
  
    static fromRootProperty(rootProperty: RootProperty, rootNode: Node | null): GraphRootProperty | undefined{
        if (!rootNode) return;
        const name = rootProperty.name;

        const graphRootProperty = new GraphRootProperty(name, rootNode);

        rootProperty.subpaths.map(subpath => {
            const graphChildProperty = GraphRootProperty.convertToGraphChildProperty(subpath, graphRootProperty, rootNode); 
            graphRootProperty.updateOrAddSubpath(graphChildProperty);
            return graphChildProperty;
        });

        return graphRootProperty;
    }

    static convertToGraphChildProperty(childProperty: ChildProperty, parent: GraphParentProperty, parentNode: Node): GraphChildProperty {
        if (childProperty instanceof SimpleProperty) 
            return GraphRootProperty.convertToGraphSimpleProperty(childProperty, parent, parentNode);
        else if (childProperty instanceof ComplexProperty) 
            return GraphRootProperty.convertToGraphComplexProperty(childProperty, parent, parentNode);
        else 
            throw new Error('Unsupported ChildProperty type');        
    }

    static convertToGraphSimpleProperty(simpleProperty: SimpleProperty, parent: GraphParentProperty, parentNode: Node): GraphSimpleProperty {
        const graphSimpleProperty =  new GraphSimpleProperty(simpleProperty.name, SequenceSignature.fromSignature(simpleProperty.signature, parentNode), parent);
        // workaround line - prevents from selecting node in the current GraphRootProperty - delete later
        graphSimpleProperty.signature.sequence.unselectAll();
        return graphSimpleProperty;
    }

    static convertToGraphComplexProperty(complexProperty: ComplexProperty, parent: GraphParentProperty, parentNode: Node): GraphComplexProperty {
        const graphComplexProperty = new GraphComplexProperty(complexProperty.name, SequenceSignature.fromSignature(complexProperty.signature, parentNode), parent);

        complexProperty.subpaths.map(subpath => {
            const graphChildProperty = GraphRootProperty.convertToGraphChildProperty(subpath, graphComplexProperty, graphComplexProperty.node);
            graphComplexProperty.updateOrAddSubpath(graphChildProperty);
        });

        // workaround line - prevents from selecting node in the current GraphRootProperty - delete later
        graphComplexProperty.signature.sequence.unselectAll();
        return graphComplexProperty;
    }
    
}
