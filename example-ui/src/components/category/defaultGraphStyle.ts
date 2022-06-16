export const style: cytoscape.Stylesheet[] = [
    {
        selector: 'node',
        style: {
            'background-color': 'white',
            'border-color': 'black',
            'border-width': '1px',
            label: 'data(label)'
        }
    },
    {
        selector: '.tag-root',
        style: {
            'background-color': 'red',
        }
    },
    {
        selector: '.availability-available',
        style: {
            'border-color': 'greenyellow',
            'border-width': '4px',
        }
    },
    {
        selector: '.availability-certainly-available',
        style: {
            'border-color': 'darkgreen',
            'border-width': '4px',
        }
    },
    {
        selector: '.availability-maybe',
        style: {
            'border-color': 'orange',
            'border-width': '4px',
        }
    },
    {
        selector: '.availability-removable',
        style: {
            'border-color': 'red',
            'border-width': '4px',
        }
    },
    {
        selector: '.selection-root',
        style: {
            'background-color': 'purple',
        }
    },
    {
        selector: '.selection-selected',
        style: {
            'background-color': 'blue',
        }
    },
    {
        selector: 'edge[label]',
        style: {
            'font-weight': 'bold',
            label: 'data(label)',
            'curve-style': 'bezier',
            'target-arrow-shape': 'triangle'
        }
    },
    {
        selector: 'edge.temporary',
        style: {
            'line-style': 'dashed',
            'line-color': 'blue'
        }
    },
    {
        selector: 'node.new',
        style: {
            'border-style': 'dashed'
        }
    },
    {
        selector: 'edge.new',
        style: {
            'line-style': 'dashed',
        }
    },
    {
        selector: 'node.no-ids[label]',
        style: {
            'color': 'red'
        }
    },
    /*
    {
        selector: 'node.coloring',
        style: {
            'border-width': '0px',
            width: '100px',
            height: '100px',
            label: '',
            events: 'no'
        }
    },
    {
        selector: 'node.mongodb',
        style: {
            'background-color': 'green',
            'background-opacity': 0.15,
        }
    },
    {
        selector: 'node.postgresql',
        style: {
            'background-color': 'red',
            'background-opacity': 0.15,
        }
    }
    */
    {
        selector: '.group-placeholder',
        style: {
            label: 'data(label)',
            events: 'no',
            opacity: 0,
        }
    },
    {
        selector: '.group',
        style: {
            label: '',
            events: 'no',
            'border-style': 'dashed',
            'shape': 'round-rectangle'
        }
    },
    {
        selector: '.mongodb',
        style: {
            'background-color': 'green',
            'background-opacity': 0.05,
            'border-color': 'green'
        }
    },
    {
        selector: '.postgresql',
        style: {
            'background-color': 'red',
            'background-opacity': 0.05,
            'border-style': 'dashed',
            'shape': 'round-rectangle',
            'border-color': 'red'
        }
    }
];
