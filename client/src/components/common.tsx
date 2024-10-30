import { type ReactNode, useEffect, useMemo, useState } from 'react';
import { Tooltip as NextuiTooltip, type TooltipProps } from '@nextui-org/react';
import clsx from 'clsx';
import { Link as ReactRouterLink, type UIMatch, useMatches, type LinkProps } from 'react-router-dom';
import { createPortal } from 'react-dom';

// The tooltip has no delay by default, so we add it here.

const TOOLTIP_DELAY = 600;

export function Tooltip(props: Readonly<Omit<TooltipProps, 'delay' | 'isOpen'>>) {
    return (
        <NextuiTooltip {...props} delay={TOOLTIP_DELAY} />
    );
}

// The NextUI link reloads the whole page. The React Router link does not, but it doesn't have any styles. So here we define a link that has the styles and accessibility of the NextUI link and the behavior of the React Router link.

const LINK_CLASS = 'relative inline-flex items-center tap-highlight-transparent outline-none data-[focus-visible=true]:z-10 data-[focus-visible=true]:outline-2 data-[focus-visible=true]:outline-focus data-[focus-visible=true]:outline-offset-2 text-medium no-underline hover:opacity-80 active:opacity-disabled transition-opacity';
const LINK_DISABLED_CLASS = LINK_CLASS + ' opacity-disabled cursor-default pointer-events-none';

export function CustomLink({ color = 'primary', className, isDisabled, ...rest }: Readonly<LinkProps & { isDisabled?: boolean }>) {
    const baseClass = isDisabled ? LINK_DISABLED_CLASS : LINK_CLASS;
    const disabled = isDisabled ? true : undefined;

    return (
        <ReactRouterLink {...rest} className={clsx(baseClass, `text-${color}`, className)} data-disabled={disabled} aria-disabled={disabled} />
    );
}

type PortalProps = {
    children?: ReactNode;
    to: string;
}

export function Portal({ children, to }: PortalProps) {
    const [ target, setTarget ] = useState(document.getElementById(to));

    useEffect(() => {
        setTarget(document.getElementById(to));
    }, [ to ]);

    return target ? createPortal(children, target) : null;
}

export const portals = {
    context: 'context-portal',
};

export type AwaitedRouteData<TData> = TData extends Record<string, unknown> ? {
    [TKey in keyof TData]: TData[TKey] extends Promise<infer TAwaitedData> ? TAwaitedData : TData[TKey];
} : TData;

export type AwaitedUIMatch<TData = unknown, THandle = unknown> = UIMatch<TData, THandle> & {
    awaited: TData extends Record<string, unknown> ? AwaitedRouteData<TData> : TData;
};

export function useAwaitedMatches(): Promise<AwaitedUIMatch[]> {
    const matches = useMatches();
    return useMemo(() => matchesToPromise(matches), [ matches ]);
}

function matchesToPromise(matches: UIMatch[]): Promise<AwaitedUIMatch[]> {

    // The matches are expected to look like this:
    // { data: Record<string, Promise<unknown> | unknown> }
    // We just simply translate all these promises to one via nested Promise.all calls.

    // TODO error handling?

    const promises = matches.map(match => {
        const data = match.data;
        if (!data || typeof data !== 'object')
            return Promise.resolve({ ...match, awaited: data });

        const routePromises = Object.values(data).filter(value => value instanceof Promise);

        return Promise.all(routePromises).then(awaitedData => {
            const awaited: Record<string, unknown> = { ...data };
            Object.keys(data).forEach((key, index) => {
                awaited[key] = awaitedData[index];
            });
            return { ...match, awaited };
        });
    });

    return Promise.all(promises);
}
