import { type JSX, type TransitionEvent, useEffect, useState } from 'react';
import { panelWidth, StyledNodeInspectorContainer } from './styled';

const Closing = 'CLOSING';
const Closed = 'CLOSED';
const Open = 'OPEN';
const Opening = 'OPENING';
type DrawerTransitionState =
  | typeof Closing
  | typeof Closed
  | typeof Open
  | typeof Opening

type NodeInspectorDrawerProps = {
  isOpen: boolean;
  children: JSX.Element;
}

export function NodeInspectorDrawer({
    isOpen,
    children,
}: NodeInspectorDrawerProps) {
    const [ transitionState, setTransitionState ] = useState<DrawerTransitionState>(
        isOpen ? Open : Closed,
    );

    useEffect(() => {
        if (isOpen) {
            if (transitionState === Closed || transitionState === Closing)
                setTransitionState(Opening);

        }
        else {
            if (transitionState === Open || transitionState === Opening)
                setTransitionState(Closing);

        }
    }, [ isOpen, transitionState ]);

    const onTransitionEnd = (event: TransitionEvent<HTMLDivElement>): void => {
        if (event.propertyName !== 'width')
            return;

        if (transitionState === Closing)
            setTransitionState(Closed);

        if (transitionState === Opening)
            setTransitionState(Open);

    };

    const drawerIsVisible =
    transitionState === Opening ||
    transitionState === Open ||
    transitionState === Closing;

    return (
        <StyledNodeInspectorContainer
            paneWidth={!isOpen ? 0 : panelWidth}
            onTransitionEnd={onTransitionEnd}
        >
            {drawerIsVisible && children}
        </StyledNodeInspectorContainer>
    );
}
