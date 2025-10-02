import { type GraphModel, type NodePair } from '@/components/adminer/graph-visualization/types/Graph';
import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';
import { type RelationshipModel } from '@/components/adminer/graph-visualization/types/Relationship';
import { ArcArrow } from '@/components/adminer/graph-visualization/utils/ArcArrow';
import { LoopArrow } from '@/components/adminer/graph-visualization/utils/LoopArrow';
import { StraightArrow } from '@/components/adminer/graph-visualization/utils/StraightArrow';
import { measureText } from '@/components/adminer/graph-visualization/utils/textMeasurement';
import { RELATIONSHIP_TYPE_FONT_SIZE_PX } from './renderers';
import { NODE_RADIUS } from '../../utils/constants';

export class PairwiseArcsRelationshipRouting {
    style: GraphStyleModel;
    canvas: HTMLCanvasElement;
    constructor(style: GraphStyleModel) {
        this.style = style;
        this.canvas = document.createElement('canvas');
    }

    measureRelationshipCaption(
        relationship: RelationshipModel,
        caption: string,
    ): number {
        const fontFamily = 'sans-serif';
        const padding = parseFloat(this.style.forRelationship(relationship).get('padding'));
        const canvas2DContext = this.canvas.getContext('2d');
        return (
            measureText(
                caption,
                fontFamily,
                relationship.captionHeight,
                (canvas2DContext!),
            )
            + padding * 2
        );
    }

    captionFitsInsideArrowShaftWidth(relationship: RelationshipModel): boolean {
        return parseFloat(this.style.forRelationship(relationship).get('shaft-width')) > relationship.captionHeight;
    }

    measureRelationshipCaptions(relationships: RelationshipModel[]): void {
        relationships.forEach(relationship => {
            relationship.captionHeight = RELATIONSHIP_TYPE_FONT_SIZE_PX;
            relationship.captionLength = this.measureRelationshipCaption(relationship, relationship.caption);
            relationship.captionLayout = (this.captionFitsInsideArrowShaftWidth(relationship) && !relationship.isLoop()) ? 'internal' : 'external';
        });
    }

    shortenCaption(
        relationship: RelationshipModel,
        caption: string,
        targetWidth: number,
    ): [string, number] {
        let shortCaption = caption || 'caption';
        while (true) {
            if (shortCaption.length <= 2)
                return [ '', 0 ];

            shortCaption = `${shortCaption.substr(0, shortCaption.length - 2)}\u2026`;
            const width = this.measureRelationshipCaption(relationship, shortCaption);
            if (width < targetWidth)
                return [ shortCaption, width ];
        }
    }

    computeGeometryForNonLoopArrows(nodePairs: NodePair[]): void {
        const square = (distance: number) => distance * distance;

        nodePairs.forEach(nodePair => {
            if (!nodePair.isLoop()) {
                const dx = nodePair.nodeA.x - nodePair.nodeB.x;
                const dy = nodePair.nodeA.y - nodePair.nodeB.y;
                const angle = ((Math.atan2(dy, dx) / Math.PI) * 180 + 360) % 360;
                const centreDistance = Math.sqrt(square(dx) + square(dy));

                nodePair.relationships.forEach(relationship => {
                    relationship.naturalAngle = relationship.target === nodePair.nodeA ? (angle + 180) % 360 : angle;
                    relationship.centreDistance = centreDistance;
                });
            }
        });
    }

    distributeAnglesForLoopArrows(
        nodePairs: NodePair[],
        relationships: RelationshipModel[],
    ): void {
        for (const nodePair of nodePairs) {
            if (nodePair.isLoop()) {
                let angles = [];
                const node = nodePair.nodeA;
                for (const relationship of relationships) {
                    if (!relationship.isLoop()) {
                        if (relationship.source === node)
                            angles.push(relationship.naturalAngle);
                        if (relationship.target === node)
                            angles.push(relationship.naturalAngle + 180);
                    }
                }
                angles = angles.map(a => (a + 360) % 360).sort((a, b) => a - b);

                if (angles.length > 0) {
                    let end, start;
                    const biggestGap = {
                        start: 0,
                        end: 0,
                    };

                    for (let i = 0; i < angles.length; i++) {
                        const angle = angles[i];
                        start = angle;
                        end = i === angles.length - 1 ? angles[0] + 360 : angles[i + 1];
                        if (end - start > biggestGap.end - biggestGap.start) {
                            biggestGap.start = start;
                            biggestGap.end = end;
                        }
                    }
                    const separation = (biggestGap.end - biggestGap.start) / (nodePair.relationships.length + 1);
                    for (let i = 0; i < nodePair.relationships.length; i++) {
                        const relationship = nodePair.relationships[i];
                        relationship.naturalAngle = (biggestGap.start + (i + 1) * separation - 90) % 360;
                    }
                }
                else {
                    const separation = 360 / nodePair.relationships.length;
                    for (let i = 0; i < nodePair.relationships.length; i++) {
                        const relationship = nodePair.relationships[i];
                        relationship.naturalAngle = i * separation;
                    }
                }
            }
        }
    }

    layoutRelationships(graph: GraphModel): void {
        const nodePairs = graph.groupedRelationships();

        this.computeGeometryForNonLoopArrows(nodePairs);
        this.distributeAnglesForLoopArrows(nodePairs, graph.relationships());

        for (const nodePair of nodePairs) {
            for (const relationship of nodePair.relationships)
                delete relationship.arrow;


            const middleRelationshipIndex = (nodePair.relationships.length - 1) / 2;
            const defaultDeflectionStep = 30;
            const maximumTotalDeflection = 150;
            const numberOfSteps = nodePair.relationships.length - 1;
            const totalDeflection = defaultDeflectionStep * numberOfSteps;

            const deflectionStep = totalDeflection > maximumTotalDeflection
                ? maximumTotalDeflection / numberOfSteps
                : defaultDeflectionStep;

            for (let i = 0; i < nodePair.relationships.length; i++) {
                const relationship = nodePair.relationships[i];
                const shaftWidth = parseFloat(this.style.forRelationship(relationship).get('shaft-width')) || 2;
                const headWidth = shaftWidth + 6;
                const headHeight = headWidth;

                if (nodePair.isLoop()) {
                    relationship.arrow = new LoopArrow(
                        NODE_RADIUS,
                        40,
                        defaultDeflectionStep,
                        shaftWidth,
                        headWidth,
                        headHeight,
                        relationship.captionHeight,
                    );
                }
                else {
                    if (i === middleRelationshipIndex) {
                        relationship.arrow = new StraightArrow(
                            NODE_RADIUS,
                            NODE_RADIUS,
                            relationship.centreDistance,
                            shaftWidth,
                            headWidth,
                            headHeight,
                            relationship.captionLayout,
                        );
                    }
                    else {
                        let deflection = deflectionStep * (i - middleRelationshipIndex);
                        if (nodePair.nodeA !== relationship.source)
                            deflection *= -1;

                        relationship.arrow = new ArcArrow(
                            NODE_RADIUS,
                            NODE_RADIUS,
                            relationship.centreDistance,
                            deflection,
                            shaftWidth,
                            headWidth,
                            headHeight,
                            relationship.captionLayout,
                        );
                    }
                }

                [ relationship.shortCaption, relationship.shortCaptionLength ] = relationship.arrow.shaftLength > relationship.captionLength
                    ? [ relationship.caption, relationship.captionLength ]
                    : this.shortenCaption(
                        relationship,
                        relationship.caption,
                        relationship.arrow.shaftLength,
                    );
            }
        }
    }
}
