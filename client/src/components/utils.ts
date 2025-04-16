import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Utility to conditionally combine Tailwind CSS classes.
 * Merges class names using `clsx` for conditional logic,
 * and resolves Tailwind class conflicts with `tailwind-merge`.
 */
export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}
