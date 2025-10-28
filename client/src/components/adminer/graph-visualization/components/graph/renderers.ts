import { type BaseType } from 'd3-selection';
import { type NodeCaptionLine, type NodeModel } from '@/components/adminer/graph-visualization/types/Node';
import { type RelationshipModel } from '@/components/adminer/graph-visualization/types/Relationship';
import { Renderer } from './Renderer';
import { NODE_RADIUS } from '../../utils/constants';
import { cn } from '@/components/utils';

const nodeRingStrokeSize = 8;

const OUTLINE_CLASS = 'svg-outline';

const nodeOutline = new Renderer<NodeModel>({
    name: 'nodeOutline',

    onGraphChange(selection, viz) {
        return selection
            .selectAll(`circle.${OUTLINE_CLASS}`)
            .data(node => [ node ])
            .join('circle')
            .attr('class', `${OUTLINE_CLASS} cursor-pointer`)
            .attr('cx', 0)
            .attr('cy', 0)
            .attr('r', NODE_RADIUS)
            .attr('fill', (node: NodeModel) => viz.style.forNode(node).get('color'))
            .attr('stroke', (node: NodeModel) => viz.style.forNode(node).get('border-color'))
            .attr('stroke-width', '2px');
    },
});

export const NODE_CAPTION_FONT_SIZE_PX = 10;
const CAPTION_CLASS = 'svg-caption';

const nodeCaption = new Renderer<NodeModel>({
    name: 'nodeCaption',

    onGraphChange(selection) {
        return (
            selection
                .selectAll(`text.${CAPTION_CLASS}`)
                .data((node: NodeModel) => node.caption)
                .join('text')
                .attr('class', `${CAPTION_CLASS} text-[10px] pointer-events-none select-none`)
                .attr('text-anchor', 'middle')
                .attr('x', 0)
                .attr('y', (line: NodeCaptionLine) => line.baseline)
                .text((line: NodeCaptionLine) => line.text)
        );
    },
});

const RING_CLASS = 'svg-ring';
export const NODE_HOVER_CLASS = '[&:hover>.svg-ring]:opacity-30 [&:hover>.svg-ring]:stroke-[#6ac6ff]';

const nodeRing = new Renderer<NodeModel>({
    name: 'nodeRing',

    onGraphChange(selection) {
        return selection
            .selectAll(`circle.${RING_CLASS}`)
            .data((node: NodeModel) => [ node ])
            .join('circle')
            .attr('class', d => cn(RING_CLASS, 'opacity-0', d.selected && 'opacity-30 stroke-[#fdcc59]'))
            .attr('cx', 0)
            .attr('cy', 0)
            .attr('stroke-width', `${nodeRingStrokeSize}px`)
            .attr('r', NODE_RADIUS + 4);
    },
});

const arrowPath = new Renderer<RelationshipModel>({
    name: 'arrowPath',

    onGraphChange(selection, viz) {
        return selection
            .selectAll(`path.${OUTLINE_CLASS}`)
            .data(rel => [ rel ])
            .join('path')
            .classed(OUTLINE_CLASS, true)
            .attr('fill', rel => viz.style.forRelationship(rel).get('color'))
            .attr('stroke', 'none');
    },

    onTick(selection) {
        return selection
            .selectAll<BaseType, RelationshipModel>('path')
            .attr('d', d => d.arrow!.outline(d.shortCaptionLength ?? 0));
    },
});

export const RELATIONSHIP_TYPE_FONT_SIZE_PX = 8;

const relationshipType = new Renderer<RelationshipModel>({
    name: 'relationshipType',

    onGraphChange(selection, viz) {
        return selection
            .selectAll('text')
            .data(rel => [ rel ])
            .join('text')
            .attr('text-anchor', 'middle')
            .attr('fill', rel => viz.style.forRelationship(rel).get('color'))
            .attr('class', 'text-[8px] pointer-events-none select-none');
    },

    onTick(selection) {
        return selection
            .selectAll<BaseType, RelationshipModel>('text')
            .attr('x', rel => rel?.arrow?.midShaftPoint?.x ?? 0)
            .attr('y', rel => (rel?.arrow?.midShaftPoint?.y ?? 0) + RELATIONSHIP_TYPE_FONT_SIZE_PX / 2 - 1)
            .attr('transform', rel => {
                if (rel.naturalAngle >= 90 && rel.naturalAngle <= 270)
                    return null;

                return `rotate(180 ${rel?.arrow?.midShaftPoint?.x ?? 0} ${
                    rel?.arrow?.midShaftPoint?.y ?? 0
                })`;
            })
            .text(rel => rel.shortCaption ?? '');
    },
});

const OVERLAY_CLASS = 'svg-overlay';

const relationshipOverlay = new Renderer<RelationshipModel>({
    name: 'relationshipOverlay',

    onGraphChange(selection) {
        return selection
            .selectAll(`path.${OVERLAY_CLASS}`)
            .data(rel => [ rel ])
            .join('path')
            .attr('class', d => cn(OVERLAY_CLASS, 'opacity-0 hover:opacity-30 hover:fill-[#6ac6ff]', d.selected && 'opacity-30 fill-[#fdcc59]'));
    },

    onTick(selection) {
        const band = 16;

        return selection
            .selectAll<BaseType, RelationshipModel>(`path.${OVERLAY_CLASS}`)
            .attr('d', d => d.arrow!.overlay(band));
    },
});

// The ring must be first so that the outline and caption are rendered on top of it (the ring has some opacity).
export const nodeRenderer = [ nodeRing, nodeOutline, nodeCaption ];

export const relationshipRenderer = [ arrowPath, relationshipType, relationshipOverlay ];
