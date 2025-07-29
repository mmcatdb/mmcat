import { Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, type SortDescriptor } from '@heroui/react';
import { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { twMerge } from 'tailwind-merge';

/**
 * Hook to manage sorting of data for HeroUI tables.
 * Using SortDescriptor for HeroUI's 2.0 Table onSortChange()
 */
export function useSortableData<T>(data: T[], initialSortDescriptor: SortDescriptor) {
    const [ sortDescriptor, setSortDescriptor ] = useState<SortDescriptor>(initialSortDescriptor);

    const sortedData = useMemo(() => {
        return [ ...data ].sort((a, b) => {
            const fieldA = a[sortDescriptor.column as keyof T];
            const fieldB = b[sortDescriptor.column as keyof T];

            if (typeof fieldA === 'string' && typeof fieldB === 'string') {
                const lowerA = fieldA.toLowerCase();
                const lowerB = fieldB.toLowerCase();
                if (lowerA < lowerB)
                    return sortDescriptor.direction === 'ascending' ? -1 : 1;
                if (lowerA > lowerB)
                    return sortDescriptor.direction === 'ascending' ? 1 : -1;
            }
            else {
                if (fieldA < fieldB)
                    return sortDescriptor.direction === 'ascending' ? -1 : 1;
                if (fieldA > fieldB)
                    return sortDescriptor.direction === 'ascending' ? 1 : -1;
            }
            return 0;
        });
    }, [ data, sortDescriptor ]);

    return { sortedData, sortDescriptor, setSortDescriptor };
}

type ConfirmationModalProps = {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    isFetching?: boolean;
    title: string;
    message: string;
    confirmButtonText?: string;
    cancelButtonText?: string;
    confirmButtonColor: 'success' | 'danger' | 'warning';
};

/*
 * Modal for confirming actions.
 */
export function ConfirmationModal({ isOpen, onClose, onConfirm, isFetching, title, message, confirmButtonText = 'Confirm', cancelButtonText = 'Cancel', confirmButtonColor }: ConfirmationModalProps) {
    return (
        <Modal isOpen={isOpen} onClose={onClose}>
            <ModalContent>
                <ModalHeader>
                    <p>{title}</p>
                </ModalHeader>
                <ModalBody>
                    <p>{message}</p>
                </ModalBody>
                <ModalFooter>
                    <Button onPress={onClose} isDisabled={isFetching}>{cancelButtonText}</Button>
                    <Button color={confirmButtonColor} onPress={onConfirm} isLoading={isFetching}>
                        {confirmButtonText}
                    </Button>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}

type EmptyStateProps = {
    message: string;
    buttonText: string;
    className?: string;
    buttonClassName?: string;
} & ({
    to: string;
} | {
    onClick: () => void;
});

/**
 * A placeholder for empty states in tables.
 */
export function EmptyState({ message, buttonText, buttonClassName, ...action }: EmptyStateProps) {
    const innerButton = (
        <Button
            className={twMerge('px-4 py-2', buttonClassName)}
            onPress={'onClick' in action ? action.onClick : undefined}
        >
            {buttonText}
        </Button>
    );

    return (
        <div className='text-center border-2 border-dashed border-default-200 p-12 rounded-xl'>
            <p className='text-lg mb-4'>{message}</p>

            {'to' in action ? (
                <Link to={action.to}>
                    {innerButton}
                </Link>
            ) : (
                <span>
                    {innerButton}
                </span>
            )}
        </div>
    );
}
