import { useState } from 'react';
import { ClickableUrls } from '../ClickableUrls';
import { AlternatingTable,CopyCell,KeyCell,StyledExpandValueButton,StyledInlineList,ValueCell } from './PropertiesTable.style';
import { ClipboardCopier } from '../ClipboardCopier';
import { ShowMoreOrAll } from '../ShowMoreOrAll/ShowMoreOrAll';
import { type VizItemProperty } from '@/components/adminer/graph-visualization/types/types';

type PropertiesViewProps = {
  visibleProperties: VizItemProperty[];
  onMoreClick: (numMore: number) => void;
  totalNumItems: number;
  moreStep: number;
}

export function PropertiesTable({
    visibleProperties,
    totalNumItems,
    onMoreClick,
    moreStep,
}: PropertiesViewProps) {
    return (<>
        <StyledInlineList>
            <AlternatingTable>
                <tbody data-testid='viz-details-pane-properties-table'>
                    {visibleProperties.map(({ key, value }) => (
                        <tr key={key} title={'String'}>
                            <KeyCell>
                                <ClickableUrls text={key} />
                            </KeyCell>
                            <ValueCell>
                                <ExpandableValue
                                    value={value}
                                    type={'String'}
                                />
                            </ValueCell>
                            <CopyCell>
                                <ClipboardCopier
                                    titleText={'Copy key and value'}
                                    textToCopy={`${key}: ${value}`}
                                    iconSize={12}
                                />
                            </CopyCell>
                        </tr>
                    ))}
                </tbody>
            </AlternatingTable>
        </StyledInlineList>
        <ShowMoreOrAll
            total={totalNumItems}
            shown={visibleProperties.length}
            moreStep={moreStep}
            onMore={onMoreClick}
        />
    </>);
}

const ELLIPSIS = '\u2026';
const MAX_LENGTH = 150;

type ExpandableValueProps = {
  value: string;
  type: string;
}

function ExpandableValue({ value, type }: ExpandableValueProps) {
    const [ expanded, setExpanded ] = useState(false);


    const handleExpandClick = () => {
        setExpanded(true);
    };

    let valueShown = expanded ? value : value.slice(0, MAX_LENGTH);
    const valueIsTrimmed = valueShown.length !== value.length;
    valueShown += valueIsTrimmed ? ELLIPSIS : '';

    return (<>
        {type.startsWith('Array') && '['}
        <ClickableUrls text={valueShown} />
        {valueIsTrimmed && (
            <StyledExpandValueButton onClick={handleExpandClick}>
                {' Show all'}
            </StyledExpandValueButton>
        )}
        {type.startsWith('Array') && ']'}
    </>);
}
