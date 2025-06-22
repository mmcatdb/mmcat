import { useState } from 'react';
import { type NodeItem, type RelationshipItem } from '@/components/adminer/graph-visualization/types/types';
import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';
import { PropertiesTable } from '@/components/adminer/graph-visualization/components/panel/PropertiesTable';
import { CopyToClipboardButton } from '@/components/CopyToClipboardButton';
import { capitalize } from '@/components/utils';

const DETAILS_PANE_STEP_SIZE = 1000;

type DetailsPaneProps = {
    vizItem: NodeItem | RelationshipItem;
    graphStyle: GraphStyleModel;
};

export function DefaultDetailsPane({ vizItem, graphStyle }: DetailsPaneProps) {
    const [ maxPropertiesCount, setMaxPropertiesCount ] = useState(DETAILS_PANE_STEP_SIZE);

    const elementIdProperty = {
        key: '<element-id>',
        value: `${vizItem.item.elementId}`,
        type: 'String',
    };

    const allItemProperties = [ elementIdProperty, ...vizItem.item.propertyList ].sort((a, b) => a.key < b.key ? -1 : 1);
    const visibleItemProperties = allItemProperties.slice(0, maxPropertiesCount);

    function handleMorePropertiesClick(numMore: number) {
        setMaxPropertiesCount(maxPropertiesCount + numMore);
    }

    return (
        <div className='h-full flex flex-col gap-3'>
            <div className='text-lg'>
                <div className='mb-2 flex items-center gap-2'>
                    <span>{`${capitalize(vizItem.type)} properties`}</span>

                    <CopyToClipboardButton
                        className='size-4'
                        textToCopy={allItemProperties
                            .map(prop => `${prop.key}: ${prop.value}`)
                            .join('\n')}
                        title='Copy all properties to clipboard'
                    />
                </div>

                <div className='flex gap-1'>
                    {vizItem.type === 'relationship' && (
                        <RelType
                            selectedRelType={{
                                propertyKeys: vizItem.item.propertyList.map(p => p.key),
                                relType: vizItem.item.type,
                            }}
                            graphStyle={graphStyle}
                        />
                    )}

                    {vizItem.type === 'node' && vizItem.item.labels.map((label: string) => {
                        return (
                            <NodeLabel
                                key={label}
                                graphStyle={graphStyle}
                                selectedLabel={{
                                    label,
                                    propertyKeys: vizItem.item.propertyList.map(p => p.key),
                                }}
                            />
                        );
                    })}
                </div>
            </div>

            <div className='h-full flex flex-col gap-3 overflow-auto'>
                <PropertiesTable
                    visibleProperties={visibleItemProperties}
                    onMoreClick={handleMorePropertiesClick}
                    moreStep={DETAILS_PANE_STEP_SIZE}
                    totalNumItems={allItemProperties.length}
                />
            </div>
        </div>
    );
}

type RelTypeProps = {
    graphStyle: GraphStyleModel;
    selectedRelType: { relType: string, propertyKeys: string[], count?: number };
};

function RelType({ selectedRelType, graphStyle }: RelTypeProps) {
    const styleForRelType = graphStyle.forRelationship({ type: selectedRelType.relType });

    return (
        <div className='px-2 py-0.5 rounded-md break-all font-semibold text-sm'
            style={{
                backgroundColor: styleForRelType.get('color'),
                color: styleForRelType.get('text-color-internal'),
            }}
        >
            {selectedRelType.count !== undefined
                ? `${selectedRelType.relType} (${selectedRelType.count})`
                : `${selectedRelType.relType}`}
        </div>
    );
}

type NodeLabelProps = {
    selectedLabel: {
        label: string;
        propertyKeys: string[];
        count?: number;
    };
    graphStyle: GraphStyleModel;
    /* The total number of nodes in returned graph */
    allNodesCount?: number | null;
};

function NodeLabel({ graphStyle, selectedLabel, allNodesCount }: NodeLabelProps) {
    const labels = selectedLabel.label === '*' ? [] : [ selectedLabel.label ];
    const graphStyleForLabel = graphStyle.forNode({ labels });
    const count = selectedLabel.label === '*' ? allNodesCount : selectedLabel.count;

    return (
        <div
            className='px-2 py-0.5 rounded-full break-all font-semibold text-sm'
            style={{
                backgroundColor: graphStyleForLabel.get('color'),
                color: graphStyleForLabel.get('text-color-internal'),
            }}
        >
            {`${selectedLabel.label}${count ? ` (${count})` : ''}`}
        </div>
    );
}
