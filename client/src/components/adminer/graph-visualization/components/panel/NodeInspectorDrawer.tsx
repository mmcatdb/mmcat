import { type JSX, type TransitionEvent, useEffect, useState } from 'react';
import { twJoin } from 'tailwind-merge';

const Closing = 'CLOSING';
const Closed = 'CLOSED';
const Open = 'OPEN';
const Opening = 'OPENING';
type DrawerTransitionState = typeof Closing | typeof Closed | typeof Open | typeof Opening;

type NodeInspectorDrawerProps = {
    isOpen: boolean;
    children: JSX.Element;
};

export function NodeInspectorDrawer({ isOpen, children }: NodeInspectorDrawerProps) {
    const [ transitionState, setTransitionState ] = useState<DrawerTransitionState>(isOpen ? Open : Closed);

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

    const isDrawerVisible = [ Opening, Open, Closing ].includes(transitionState);

    return (
        <div className={twJoin('absolute z-10 right-2 top-2 bottom-2 max-w-9/10 bg-default-50 overflow-hidden overflow-y-auto transition-width', isOpen ? 'w-80' : 'w-0')}
            onTransitionEnd={onTransitionEnd}
        >
            {isDrawerVisible && children}
        </div>
    );
}
