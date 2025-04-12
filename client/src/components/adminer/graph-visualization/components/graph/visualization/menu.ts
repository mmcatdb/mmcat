import { type BaseType, type Selection } from 'd3-selection';
import { arc as d3Arc } from 'd3-shape';
import { type NodeModel } from '@/components/adminer/graph-visualization/types/Node';
import { Renderer } from './Renderer';
import { type Visualization } from './Visualization';
import { menuIcons } from '@/components/adminer/graph-visualization/components/Icons';

const noOp = () => undefined;

const numberOfItemsInContextMenu = 3;

function drawArc(radius: number, itemNumber: number, width = 30) {
    const startAngle =
    ((2 * Math.PI) / numberOfItemsInContextMenu) * (itemNumber - 1);
    const endAngle = startAngle + (2 * Math.PI) / numberOfItemsInContextMenu;
    const innerRadius = Math.max(radius + 8, 20);
    return d3Arc()
        .innerRadius(innerRadius)
        .outerRadius(innerRadius + width)
        .startAngle(startAngle)
        .endAngle(endAngle)
        .padAngle(0.03);
}

const getSelectedNode = (node: NodeModel) => (node.selected ? [ node ] : []);

const attachContextEvent = (
    eventType: string,
    elements: [
    Selection<BaseType | SVGPathElement, NodeModel, BaseType, NodeModel>,
    Selection<BaseType | SVGGElement, NodeModel, BaseType, NodeModel>
  ],
    viz: Visualization,
    content: string,
    label: string,
) => {
    elements.forEach(element => {
        element.on('mousedown.drag', (event: Event) => {
            event.stopPropagation();
            return null;
        });
        element.on('mouseup', (_event: Event, node: NodeModel) =>
            viz.trigger(eventType, node),
        );
        element.on('mouseover', (_event: Event, node: NodeModel) => {
            node.contextMenu = {
                menuSelection: eventType,
                menuContent: content,
                label,
            };
            viz.trigger('menuMouseOver', node);
        });

        element.on('mouseout', (_event: Event, node: NodeModel) => {
            delete node.contextMenu;
            viz.trigger('menuMouseOut', node);
        });
    });
};

function createMenuItem(
    selection: Selection<SVGGElement, NodeModel, BaseType, unknown>,
    viz: Visualization,
    eventType: string,
    itemIndex: number,
    className: string,
    position: [number, number],
    svgIconKey: 'Expand / Collapse' | 'Unlock' | 'Remove',
    tooltip: string,
) {
    const tab = selection
        .selectAll(`path.${className}`)
        .data(getSelectedNode)
        .join('path')
        .classed(className, true)
        .classed('context-menu-item', true)
        .attr('d', node => {
            // @ts-expect-error Expected 1-2 arguments, but got 0.ts(2554)
            return drawArc(node.radius, itemIndex, 1)();
        });

    const rawSvgIcon = menuIcons[svgIconKey];
    const svgIcon = document.importNode(
    new DOMParser().parseFromString(rawSvgIcon, 'application/xml')
        .documentElement.firstChild as HTMLElement,
    true,
    );
    const icon = selection
        .selectAll(`.icon.${className}`)
        .data(getSelectedNode)
        .join('g')
        .html(svgIcon.innerHTML)
        .classed('icon', true)
        .classed(className, true)
        .classed('context-menu-item', true)
        .attr('transform', (node: NodeModel) => {
            return `translate(${Math.floor(
                // @ts-expect-error
                drawArc(node.radius, itemIndex).centroid()[0] +
          (position[0] * 100) / 100,
            )},${Math.floor(
                // @ts-expect-error
                drawArc(node.radius, itemIndex).centroid()[1] +
          (position[1] * 100) / 100,
            )}) scale(0.7)`;
        })
        .attr('color', (node: NodeModel) => {
            return viz.style.forNode(node).get('text-color-internal');
        });

    attachContextEvent(eventType, [ tab, icon ], viz, tooltip, rawSvgIcon);

    tab
        // @ts-expect-error
        .transition()
        .duration(200)
        .attr('d', (node: NodeModel) => {
            // @ts-expect-error Expected 1-2 arguments, but got 0.ts(2554)
            return drawArc(node.radius, itemIndex)();
        })
        .selection()
        .exit<NodeModel>()
        .transition()
        .duration(200)
        .attr('d', (node: NodeModel) => {
            // @ts-expect-error Expected 1-2 arguments, but got 0.ts(2554)
            return drawArc(node.radius, itemIndex, 1)();
        })
        .remove();

    return icon;
}

const donutRemoveNode = new Renderer<NodeModel>({
    name: 'donutRemoveNode',
    onGraphChange(selection, viz) {
        return createMenuItem(
            selection,
            viz,
            'nodeClose',
            1,
            'remove-node',
            [ -8, 0 ],
            'Remove',
            'Dismiss',
        );
    },

    onTick: noOp,
});

const donutExpandNode = new Renderer<NodeModel>({
    name: 'donutExpandNode',
    onGraphChange(selection, viz) {
        return createMenuItem(
            selection,
            viz,
            'nodeDblClicked',
            2,
            'expand-node',
            [ -8, -10 ],
            'Expand / Collapse',
            'Expand / Collapse child relationships',
        );
    },

    onTick: noOp,
});

const donutUnlockNode = new Renderer<NodeModel>({
    name: 'donutUnlockNode',
    onGraphChange(selection, viz) {
        return createMenuItem(
            selection,
            viz,
            'nodeUnlock',
            3,
            'unlock-node',
            [ -10, -6 ],
            'Unlock',
            'Unlock the node to re-layout the graph',
        );
    },

    onTick: noOp,
});

export const nodeMenuRenderer = [
    donutExpandNode,
    donutRemoveNode,
    donutUnlockNode,
];
