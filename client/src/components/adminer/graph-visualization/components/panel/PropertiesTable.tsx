import { useState } from 'react';
import { ClickableUrls } from './ClickableUrls';
import { type VizItemProperty } from '@/components/adminer/graph-visualization/types/types';
import { CopyToClipboardButton } from '@/components/CopyToClipboardButton';

type PropertiesViewProps = {
    visibleProperties: VizItemProperty[];
    onMoreClick: (numMore: number) => void;
    totalNumItems: number;
    moreStep: number;
};

export function PropertiesTable({ visibleProperties, totalNumItems, onMoreClick, moreStep }: PropertiesViewProps) {
    return (<>
        <ul className='break-all list-none'>
            <table className='w-full text-sm'>
                <tbody>
                    {visibleProperties.map(({ key, value }) => (
                        <tr key={key} title='String' className='even:bg-default-200'>
                            <td className='w-[30%] p-0.5 font-semibold align-top'>
                                <ClickableUrls text={key} />
                            </td>
                            <td className='p-0.5 align-top whitespace-pre-wrap'>
                                <ExpandableValue value={value} type='String' />
                            </td>
                            <td className='p-0 flex justify-end'>
                                <CopyToClipboardButton
                                    className='w-6 h-5 px-1 py-0.5 flex'
                                    textToCopy={`${key}: ${value}`}
                                    title='Copy key and value'
                                />
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </ul>

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
};

function ExpandableValue({ value, type }: ExpandableValueProps) {
    const [ expanded, setExpanded ] = useState(false);

    let valueShown = expanded ? value : value.slice(0, MAX_LENGTH);
    const valueIsTrimmed = valueShown.length !== value.length;
    valueShown += valueIsTrimmed ? ELLIPSIS : '';

    return (<>
        {type.startsWith('Array') && '['}
        <ClickableUrls text={valueShown} />
        {valueIsTrimmed && (
            <button onClick={() => setExpanded(true)} className='pl-1 text-primary-500 hover:text-primary-700'>
                Show all
            </button>
        )}
        {type.startsWith('Array') && ']'}
    </>);
}

type ShowMoreOrAllProps = {
    total: number;
    shown: number;
    moreStep: number;
    onMore: (num: number) => void;
};

function ShowMoreOrAll({ total, shown, moreStep, onMore }: ShowMoreOrAllProps) {
    if (shown >= total)
        return null;

    const numMore = Math.min(moreStep, total - shown);

    return  (
        <div>
            <button onClick={() => onMore(numMore)} className='text-primary-500 hover:text-primary-700'>
                {`Show ${numMore} more`}
            </button>
            &nbsp;|&nbsp;
            <button onClick={() => onMore(total)} className='text-primary-500 hover:text-primary-700'>
                {`Show all`}
            </button>
        </div>
    );
}
