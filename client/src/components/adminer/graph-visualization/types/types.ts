import { type NodeModel } from './Node';
import { type RelationshipModel } from './Relationship';

export type BasicNode = {
    id: string;
    elementId: string;
    labels: string[];
    properties: Record<string, string>;
  };

export type BasicRelationship = {
    id: string;
    elementId: string;
    startNodeId: string;
    endNodeId: string;
    type: string;
    properties: Record<string, string>;
    startNodeLabel: string[];
    endNodeLabel: string[];
    startNodeProperties: Record<string, string>;
    endNodeProperties: Record<string, string>;
  };

export type BasicNodesAndRels = {
    nodes: BasicNode[];
    relationships: BasicRelationship[];
  };

export type DeduplicatedBasicNodesAndRels = {
    nodes: BasicNode[];
    relationships: BasicRelationship[];
    limitHit?: boolean;
  };

export type VizItemProperty = {
    key: string;
    value: string;
};

export type VizItem = NodeItem | RelationshipItem | CanvasItem | StatusItem;

export type NodeItem = {
    type: 'node';
    item: Pick<NodeModel, 'id' | 'elementId' | 'labels' | 'propertyList'>;
};

type StatusItem = {
    type: 'status-item';
    item: string;
}

export type RelationshipItem = {
    type: 'relationship';
    item: Pick<RelationshipModel, 'id' | 'elementId' | 'type' | 'propertyList'>;
};

type CanvasItem = {
    type: 'canvas';
    item: {
        nodeCount: number;
        relationshipCount: number;
    };
};

export type ZoomLimitsReached = {
    zoomInLimitReached: boolean;
    zoomOutLimitReached: boolean;
};

export enum ZoomType {
    IN = 'in',
    OUT = 'out',
    FIT = 'fit'
}

export type GetNodeNeighborsFn = (
    node: BasicNode | NodeModel,
    currentNeighborIds: string[],
    callback: (data: BasicNodesAndRels) => void
) => void;
