type GroupDefinition = {
    id: string;
    background: string;
    color: string;
};

const groupDefinitions: GroupDefinition[] = [
    {
        id: 'postgresql-0',
        background: '#e1d5e7',
        color: '#9673a6',
    },
    {
        id: 'mongodb-0',
        background: '#d5e8d4',
        color: '#82b366',
    },
    {
        id: 'neo4j-0',
        background: '#dae8fc',
        color: '#6c8ebf',
    },
    {
        id: 'postgresql-1',
        background: '#b89cc6',
        color: '#795789',
    },
    {
        id: 'mongodb-1',
        background: '#9cc99a',
        color: '#66964a',
    },
    {
        id: 'neo4j-1',
        background: '#83b1f4',
        color: '#486fa6',
    },
];

export function groupHighlightColorToClass(id: string, type: 'root' | 'property'): string {
    return id + '-' + type;
}

export const groupColors = groupDefinitionsToColors(groupDefinitions);

function groupDefinitionsToColors(groups: GroupDefinition[]) {
    const root: Record<string, string> = {};
    groups.forEach(group => root[group.id] = group.color);

    const property: Record<string, string> = {};
    groups.forEach(group => property[group.id] = group.background);

    return { root, property };
}

function groupDefinitionsToStyle(groups: GroupDefinition[]): cytoscape.Stylesheet[] {
    return groups.map((group, index) => ({
        selector: '.group-' + group.id,
        style: {
            'background-color': group.background,
            'color': group.color,
            'border-color': group.color,
            'padding-right': ((index + 2) * 4) + 'px',
        },
    }));
}

export const style: cytoscape.Stylesheet[] = [
    {
        selector: 'node',
        style: {
            'background-color': 'white',
            'border-color': 'black',
            'border-width': '1px',
            label: 'data(label)',
        },
    },
    {
        selector: 'node.highlighted',
        style: {
            'background-color': '#ffcc00',
            'border-color': '#ff9900',    
            'border-width': '3px',        
        },
    },
    {
        selector: '.tag-root',
        style: {
            'background-color': 'red',
        },
    },
    {
        selector: '.availability-available',
        style: {
            'border-color': 'greenyellow',
            'border-width': '4px',
        },
    },
    {
        selector: '.availability-certainly-available',
        style: {
            'border-color': 'darkgreen',
            'border-width': '4px',
        },
    },
    {
        selector: '.availability-ambiguous',
        style: {
            'border-color': 'orange',
            'border-width': '4px',
        },
    },
    {
        selector: '.availability-removable',
        style: {
            'border-color': 'red',
            'border-width': '4px',
        },
    },
    {
        selector: '.selection-root',
        style: {
            'background-color': 'purple',
        },
    },
    {
        selector: '.selection-selected',
        style: {
            'background-color': 'blue',
        },
    },
    {
        selector: 'edge[label]',
        style: {
            'font-weight': 'bold',
            label: 'data(label)',
            'curve-style': 'bezier',
            'target-arrow-shape': 'triangle-backcurve',
            'arrow-scale': 2,
        },
    },
    {
        selector: 'edge.temporary',
        style: {
            'line-style': 'dashed',
            'line-color': 'blue',
        },
    },
    {
        selector: 'edge.isa',
        style: {
            'line-color': 'green',
        },
    },
    {
        selector: 'edge.role',
        style: {
            'line-color': 'orange',
        },
    },
    {
        selector: 'node.new',
        style: {
            'border-style': 'dashed',
        },
    },
    {
        selector: 'edge.new',
        style: {
            'line-style': 'dashed',
        },
    },
    {
        selector: 'node.no-ids[label]',
        style: {
            'color': 'red',
        },
    },
    {
        selector: '.group-placeholder',
        style: {
            label: '',
            events: 'no',
            width: '44px',
            height: '44px',
            'z-compound-depth': 'bottom',
            'border-width': '0px',
        },
    },
    {
        selector: '.group-placeholder-hidden',
        style: {
            opacity: 0,
        },
    },
    {
        selector: '.group',
        style: {
            label: 'data(label)',
            events: 'no',
            'shape': 'round-rectangle',
            'border-style': 'dashed',
            'border-width': '2px',
            'font-weight': 'bold',
            'background-opacity': 0.01,
        },
    },
    ...groupDefinitionsToStyle(groupDefinitions),
    ...Object.entries(groupColors.root).map(([ id, color ]) => ({
        selector: '.' + groupHighlightColorToClass(id, 'root'),
        style: {
            'background-color': color,
            'border-width': '1px',
            'border-color': 'black',
        },
    })),
    ...Object.entries(groupColors.property).map(([ id, color ]) => ({
        selector: '.' + groupHighlightColorToClass(id, 'property'),
        style: {
            'background-color': color,
            'border-width': '1px',
            'border-color': groupColors.root[id],
        },
    })),
];
