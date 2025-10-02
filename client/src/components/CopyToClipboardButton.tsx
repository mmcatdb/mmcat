import { DocumentDuplicateIcon } from '@heroicons/react/24/outline';
import { type ReactNode } from 'react';
import { toast } from 'react-toastify';
import { cn } from '@/components/utils';

type CopyToClipboardButtonProps = {
    textToCopy: string | (() => string);
    title: string | undefined;
    className?: string;
    children?: ReactNode;
};

export function CopyToClipboardButton({ textToCopy, title, className, children }: CopyToClipboardButtonProps) {
    return (
        <button
            onClick={() => copyToClipboard(textToCopy)}
            title={title}
            className={cn('cursor-pointer', className)}
        >
            {children !== undefined ? children : <DocumentDuplicateIcon />}
        </button>
    );
}

async function copyToClipboard(textToCopy: string | (() => string)): Promise<void> {
    const text = typeof textToCopy === 'function' ? textToCopy() : textToCopy;
    await navigator.clipboard.writeText(text);
    toast.success('Copied to clipboard!');
}
