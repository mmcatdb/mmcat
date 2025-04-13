import { NonClickableLabelChip } from './styled';
import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';

export type NodeLabelProps = {
  selectedLabel: {
    label: string;
    propertyKeys: string[];
    count?: number;
  };
  graphStyle: GraphStyleModel;
  /* The total number of nodes in returned graph */
  allNodesCount?: number | null;
}

export function NodeLabel({
    graphStyle,
    selectedLabel,
    allNodesCount,
}: NodeLabelProps) {
    const labels = selectedLabel.label === '*' ? [] : [ selectedLabel.label ];
    const graphStyleForLabel = graphStyle.forNode({
        labels,
    });
    const count =
    selectedLabel.label === '*' ? allNodesCount : selectedLabel.count;

    return (
        <NonClickableLabelChip
            style={{
                backgroundColor: graphStyleForLabel.get('color'),
                color: graphStyleForLabel.get('text-color-internal'),
            }}
        >
            {`${selectedLabel.label}${count || count === 0 ? ` (${count})` : ''}`}
        </NonClickableLabelChip>
    );
}
