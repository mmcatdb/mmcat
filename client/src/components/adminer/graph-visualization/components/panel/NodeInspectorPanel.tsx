import { type JSX, Component } from 'react';
import { DefaultDetailsPane } from './DefaultPanelContent/DefaultDetailsPane';
import { NodeInspectorDrawer } from './NodeInspectorDrawer';
import { PaneContainer, StyledNodeInspectorTopMenuChevron } from './styled';
import { type GraphStyleModel } from '../../types/GraphStyle';
import { type VizItem } from '../../types/types';
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/solid';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { type GraphResponse } from '@/types/adminer/DataResponse';

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
            <StyledNodeInspectorTopMenuChevron
                expanded={expanded}
                onClick={() => this.setState({ expanded: !expanded })}
                title={expanded ? 'Collapse the node properties display' : 'Expand the node properties display'}
                aria-label={expanded ? 'Collapse the node properties display' : 'Expand the node properties display'}
            >
                {expanded ? <ChevronRightIcon /> : <ChevronLeftIcon />}
            </StyledNodeInspectorTopMenuChevron>

            <NodeInspectorDrawer isOpen={expanded}>
                <PaneContainer>
                    {shownEl.type === 'node' || shownEl.type === 'relationship' ? (
                        <DefaultDetailsPane
                            vizItem={shownEl}
                            graphStyle={graphStyle}
                        />
                    ) : (
                        <DatabaseDocument
                            data={this.props.data}
                            kindReferences={[]}
                            kind={''}
                            datasourceId={''}
                            datasources={[]}
                        />
                    )}
                </PaneContainer>
            </NodeInspectorDrawer>
        </>);
    }
}
