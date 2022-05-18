export const style: cytoscape.Stylesheet[] = [
    {
        selector: 'node',
        style: {
            'background-color': 'white',
            'border-color': 'black',
            'border-width': '1px',
            label: 'data(label)',
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
        selector: '.availability-certainlyAvailable',
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
        selector: "edge[label]",
        style: {
            "font-weight": "bold",
            label: 'data(label)',
        }
    },
    {
        selector: 'edge.temporary',
        style: {
            "line-style": "dashed",
            "line-color": "blue"
        }
    },
    {
        selector: 'node.new',
        style: {
            "border-style": "dashed"
        }
    },
    {
        selector: 'edge.new',
        style: {
            "line-style": "dashed",
        }
    },
    {
        selector: 'node.no-ids[label]',
        style: {
            "color": "red"
        }
    }
];
