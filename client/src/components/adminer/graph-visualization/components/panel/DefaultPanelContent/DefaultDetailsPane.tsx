import { useState } from 'react';
import { type NodeItem, type RelationshipItem } from '@/components/adminer/graph-visualization/types/types';
import { PaneBody, PaneHeader, PaneTitle, PaneWrapper } from './styled';
import { NodeLabel } from './NodeLabel';
import { RelType } from './RelType';
import { type GraphStyleModel } from '@/components/adminer/graph-visualization/types/GraphStyle';
import { ClipboardCopier } from '@/components/adminer/graph-visualization/components/panel/ClipboardCopier';
import { PropertiesTable } from '@/components/adminer/graph-visualization/components/panel/PropertiesTable/PropertiesTable';
import { capitalize } from '@/components/adminer/graph-visualization/utils/utils';

export const DETAILS_PANE_STEP_SIZE = 1000;

type DetailsPaneProps = {
  vizItem: NodeItem | RelationshipItem;
  graphStyle: GraphStyleModel;
}

export function DefaultDetailsPane({
    vizItem,
    graphStyle,
}: DetailsPaneProps) {
    const [ maxPropertiesCount, setMaxPropertiesCount ] = useState(
        DETAILS_PANE_STEP_SIZE,
    );

    const elementIdProperty = {
        key: '<element-id>',
        value: `${vizItem.item.elementId}`,
        type: 'String',
    };
    const allItemProperties = [
        elementIdProperty,
        ...vizItem.item.propertyList,
    ].sort((a, b) => (a.key < b.key ? -1 : 1));
    const visibleItemProperties = allItemProperties.slice(0, maxPropertiesCount);

    const handleMorePropertiesClick = (numMore: number) => {
        setMaxPropertiesCount(maxPropertiesCount + numMore);
    };

    return (
        <PaneWrapper>
            <PaneHeader>
                <PaneTitle>
                    <span>{`${capitalize(vizItem.type)} properties`}</span>
                    <ClipboardCopier
                        textToCopy={allItemProperties
                            .map(prop => `${prop.key}: ${prop.value}`)
                            .join('\n')}
                        titleText='Copy all properties to clipboard'
                        iconSize={10}
                    />
                </PaneTitle>
                {vizItem.type === 'relationship' && (
                    <RelType
                        selectedRelType={{
                            propertyKeys: vizItem.item.propertyList.map(p => p.key),
                            relType: vizItem.item.type,
                        }}
                        graphStyle={graphStyle}
                    />
                )}
                {vizItem.type === 'node' &&
          vizItem.item.labels.map((label: string) => {
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
            </PaneHeader>
            <PaneBody>
                <PropertiesTable
                    visibleProperties={visibleItemProperties}
                    onMoreClick={handleMorePropertiesClick}
                    moreStep={DETAILS_PANE_STEP_SIZE}
                    totalNumItems={allItemProperties.length}
                />
            </PaneBody>
        </PaneWrapper>
    );
}
