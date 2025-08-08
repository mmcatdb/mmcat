import { type JSX, Component } from 'react';
import { DefaultDetailsPane } from './DefaultDetailsPane';
import { NodeInspectorDrawer } from './NodeInspectorDrawer';
import { type GraphStyleModel } from '../../types/GraphStyle';
import { type VizItem } from '../../types/types';
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/solid';
import { DocumentView } from '@/components/adminer/dataView/DocumentView';
import { type GraphResponse } from '@/types/adminer/DataResponse';
import { type KindReference } from '@/types/adminer/AdminerReferences';
import { type Datasource } from '@/types/Datasource';

type NodeInspectorPanelProps = {
    graphStyle: GraphStyleModel;
    hoveredItem: VizItem;
    selectedItem: VizItem;
    data: GraphResponse;
};

type NodeInspectorPanelState = {
    expanded: boolean;
};

export class NodeInspectorPanel extends Component<NodeInspectorPanelProps, NodeInspectorPanelState> {
    constructor(props: NodeInspectorPanelProps) {
        super(props);

        this.state ={
            expanded: true,
        };
    }

    render(): JSX.Element {
        const {
            graphStyle,
            hoveredItem,
            selectedItem,
        } = this.props;

        const { expanded } = this.state;

        const relevantItems = [ 'node', 'relationship' ];
        const shownEl = (hoveredItem && relevantItems.includes(hoveredItem.type)) ? hoveredItem : selectedItem;

        return (<>
            <button
                className='absolute z-20 right-2 top-2 size-8 p-1 bg-default-50'
                onClick={() => this.setState({ expanded: !expanded })}
                title={expanded ? 'Collapse the node properties display' : 'Expand the node properties display'}
                aria-label={expanded ? 'Collapse the node properties display' : 'Expand the node properties display'}
            >
                {expanded ? <ChevronRightIcon /> : <ChevronLeftIcon />}
            </button>

            <NodeInspectorDrawer isOpen={expanded}>
                <div className='w-80 h-full flex flex-col p-4'>
                    {shownEl.type === 'node' || shownEl.type === 'relationship' ? (
                        <DefaultDetailsPane
                            vizItem={shownEl}
                            graphStyle={graphStyle}
                        />
                    ) : (
                        <DocumentView
                            data={this.props.data}
                            kindReferences={EMPTY_REFERENCES}
                            kind=''
                            datasourceId=''
                            datasources={EMPTY_DATASOURCES}
                        />
                    )}
                </div>
            </NodeInspectorDrawer>
        </>);
    }
}

// FIXME Use real values? Or edit the database document to not require these?
const EMPTY_REFERENCES: KindReference[] = [];
const EMPTY_DATASOURCES: Datasource[] = [];
