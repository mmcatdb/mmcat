import { type Signature } from '@/types/identifiers';
import { cn } from './utils';

type SignatureDisplayProps = {
    signature: Signature;
    className?: string;
};

export function SignatureDisplay({ signature, className }: SignatureDisplayProps) {
    return (
        <span className={cn('h-5 px-1 inline-block rounded-sm bg-default-200 text-foreground font-mono font-semibold', className)}>
            {signature.toString()}
        </span>
    );
}
