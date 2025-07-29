import { type ReactNode, useLayoutEffect, useState } from 'react';
import { Card, CardBody, Tooltip as HeroUITooltip, type TooltipProps } from '@heroui/react';
import { Link as ReactRouterLink, type LinkProps } from 'react-router-dom';
import { createPortal } from 'react-dom';
import { HiXMark } from 'react-icons/hi2';
import { twMerge } from 'tailwind-merge';

/** The tooltip has no delay by default, so we add it here. */
// The delay is in milliseconds.

const TOOLTIP_DELAY = 100;

/**
 * Wrapper around HeroUI Tooltip with a default delay.
 */
export function Tooltip(props: Omit<TooltipProps, 'delay' | 'isOpen'>) {
    return (
        <HeroUITooltip {...props} delay={TOOLTIP_DELAY} />
    );
}

// The HeroUI link reloads the whole page. The React Router link does not, but it doesn't have any styles. So here we define a link that has the styles and accessibility of the HeroUI link and the behavior of the React Router link.

const LINK_CLASS = 'relative inline-flex items-center tap-highlight-transparent outline-hidden data-[focus-visible=true]:z-10 data-[focus-visible=true]:outline-2 data-[focus-visible=true]:outline-focus data-[focus-visible=true]:outline-offset-2 text-medium no-underline hover:opacity-80 active:opacity-disabled transition-opacity';
const LINK_DISABLED_CLASS = LINK_CLASS + ' opacity-disabled cursor-default pointer-events-none';

export function CustomLink({ className, isDisabled, ...rest }: LinkProps & { isDisabled?: boolean }) {
    const baseClass = isDisabled ? LINK_DISABLED_CLASS : LINK_CLASS;
    const disabled = isDisabled ? true : undefined;

    return (
        <ReactRouterLink {...rest} className={twMerge(baseClass, className)} data-disabled={disabled} aria-disabled={disabled} />
    );
}

type PortalProps = {
    children?: ReactNode;
    to: string;
};

export function Portal({ children, to }: PortalProps) {
    const [ target, setTarget ] = useState(document.getElementById(to));

    useLayoutEffect(() => {
        setTarget(document.getElementById(to));
    }, [ to ]);

    return target ? createPortal(children, target) : null;
}

/**
 * IDs for predefined portal targets.
 */
export const portals = {
    context: 'context-portal',
};

type InfoBannerProps = {
    /** The content of the banner. */
    children: ReactNode;
    /** Additional classes to apply to the banner. */
    className?: string;
    /** A function to call when the banner is dismissed. */
    dismissBanner: () => void;
};

/**
 * A reusable banner card with content to be provided via children.
 */
export function InfoBanner({ children, className, dismissBanner }: InfoBannerProps) {
    return (
        <Card shadow='sm' radius='lg' className={twMerge('relative bg-content1', className)}>
            <CardBody className='text-sm text-foreground px-4 py-3 relative'>
                <button
                    onClick={dismissBanner}
                    className='absolute top-2 right-2 text-default-500 hover:text-foreground transition'
                >
                    <HiXMark className='w-5 h-5' />
                </button>
                {children}
            </CardBody>
        </Card>
    );
}
