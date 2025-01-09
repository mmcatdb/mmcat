import type { FC, SVGProps } from 'react';

export type AddIconProps = SVGProps<SVGSVGElement>;

export const AddIcon: FC<AddIconProps> = (props) => (
    <svg
        aria-hidden='true'
        fill='none'
        focusable='false'
        height='1em'
        role='presentation'
        viewBox='0 0 24 24'
        width='1em'
        {...props}
    >
        <path
            d='M12 5V19'
            stroke='currentColor'
            strokeWidth={2}
            strokeLinecap='round'
            strokeLinejoin='round'
        />
        <path
            d='M5 12H19'
            stroke='currentColor'
            strokeWidth={2}
            strokeLinecap='round'
            strokeLinejoin='round'
        />
    </svg>
);
