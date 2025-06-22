import { DocumentDuplicateIcon } from '@heroicons/react/24/outline';
import { toast } from 'react-toastify';

type CopyToClipboardButtonProps = {
    textToCopy: string;
    title: string;
    className?: string;
};

export function CopyToClipboardButton({ textToCopy, title, className }: CopyToClipboardButtonProps) {
    return (
        <button
            onClick={() => copyToClipboard(textToCopy)}
            title={title}
            className={className}
        >
            <DocumentDuplicateIcon />
        </button>
    );
}

async function copyToClipboard(text: string): Promise<void> {
    await navigator.clipboard.writeText(text);
    toast.success('Copied to clipboard!');
}
