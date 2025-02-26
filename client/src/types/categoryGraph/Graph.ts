import type { Id } from '../id';

type GroupData = any;

// type GraphControl = {
//     nodes: ComparableMap<Key, number, Node>;
//     edges: ComparableMap<Signature, string, Edge>;
// };

/** @deprecated */
export type Group = GroupData & {
    // node: NodeSingular;
};

/** @deprecated */
export type GraphHighlightState = {
    groupId: string;
    mappingIds?: Id[];
} | undefined;

// class GraphHighlights {
//     private readonly _groups = new Map<string, Group>();
//     /** Reactive accessor */
//     // readonly groups = shallowRef<Group[]>([]);
//     private availableGroups: GroupData[] = [];

//     constructor(
//         private readonly control: GraphControl,
//     ) {
//         control.cytoscape.on('tap', 'node', (event: EventObject) => {
//             const node = (event.target as NodeSingular).data('schemaData') as Node;
//             this.clickNode(node);
//         });
//     }

//     reset(availableGroups: GroupData[]) {
//         this.availableGroups = availableGroups;
//         this._groups.clear();
//         this.groups.value = [];
//     }

//     // API

//     getOrCreateGroup(groupId: string): Group {
//         const group = this._groups.get(groupId);
//         if (group)
//             return group;

//         const groupData = this.availableGroups.find(group => group.id === groupId);
//         if (!groupData)
//             throw new Error('Group not found: ' + groupId);

//         const id = groupData.id;

//         const newGroup = {
//             ...groupData,
//             node: this.control.cytoscape.add({
//                 data: {
//                     id: 'group_' + id,
//                     label: groupData.logicalModel.datasource.label,
//                 },
//                 classes: 'group ' + 'group-' + id,
//             }),
//         };

//         this._groups.set(groupId, newGroup);
//         this.groups.value = [ ...this._groups.values() ];

//         return newGroup;
//     }

//     clickGroup(groupId: string): GraphHighlightState {
//         if (this.state?.groupId === groupId)
//             this.setState(undefined);
//         else
//             this.setState({ groupId });

//         return this.state;
//     }

//     clickNode(node: Node): GraphHighlightState {
//         if (!this.state)
//             return;

//         const mappings = this._groups.get(this.state.groupId)?.mappings.filter(mapping => mapping.root.key.equals(node.schemaObjex.key));
//         if (!mappings || mappings.length === 0)
//             // The node is not a root of any mapping in the current active group.
//             return;

//         if (this.state.mappingIds?.includes(mappings[0].mapping.id))
//             // Deactivate the current mapping.
//             this.setState({ groupId: this.state.groupId });
//         else
//             // Activate a new mapping.
//             this.setState({ groupId: this.state.groupId, mappingIds: mappings.map(m => m.mapping.id) });
//     }

//     // Inner state logic

//     private state: GraphHighlightState = undefined;

//     private setState(newState: GraphHighlightState) {
//         this.toggleState(this.state, false);
//         this.toggleState(newState, true);
//         this.state = newState;
//     }

//     private toggleState(state: GraphHighlightState, value: boolean) {
//         if (!state)
//             return;

//         if (!state.mappingIds) {
//             this.toggleGroup(state.groupId, value);
//             return;
//         }

//         state.mappingIds.map(id => this.toggleMapping(state.groupId, id, value));
//     }

//     private toggleGroup(groupId: string, value: boolean) {
//         const group = this._groups.get(groupId);
//         if (!group)
//             return;

//         console.log(group);

//         group.mappings.forEach(mapping => {
//             if (!mapping.root)
//                 return;

//             this.control.nodes.get(mapping.root.key)?.highlights.select(group.id, 'root', value);
//         });
//     }

//     private toggleMapping(groupId: string, mappingId: Id, value: boolean) {
//         const mapping = this._groups.get(groupId)?.mappings.find(mapping => mapping.mapping.id === mappingId);
//         if (!mapping)
//             return;

//         this.control.nodes.get(mapping.root.key)?.highlights.select(groupId, 'root', value);
//         mapping.properties.forEach(property => this.control.nodes.get(property.key)?.highlights.select(groupId, 'property', value));
//     }
// }
