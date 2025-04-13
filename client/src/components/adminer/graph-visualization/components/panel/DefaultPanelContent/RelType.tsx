import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';
import { NonClickableRelTypeChip } from './styled';

export type RelTypeProps = {
  graphStyle: GraphStyleModel;
  selectedRelType: { relType: string, propertyKeys: string[], count?: number };
}

export function RelType({
    selectedRelType,
    graphStyle,
}: RelTypeProps) {
    const styleForRelType = graphStyle.forRelationship({
        type: selectedRelType.relType,
    });
    return (
        <NonClickableRelTypeChip
            style={{
                backgroundColor: styleForRelType.get('color'),
                color: styleForRelType.get('text-color-internal'),
            }}
        >
            {selectedRelType.count !== undefined
                ? `${selectedRelType.relType} (${selectedRelType.count})`
                : `${selectedRelType.relType}`}
        </NonClickableRelTypeChip>
    );
}
