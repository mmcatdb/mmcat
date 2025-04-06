import { Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader } from '@nextui-org/react';
import { type SortDescriptor } from '@react-types/shared';
import { useState } from 'react';

// Sorting of specified data
// using SortDescriptor for NextUI's 2.0 Table onSortChange()
// hours wasted: 5
export function useSortableData<T>(data: T[], initialSortDescriptor: SortDescriptor) {
    const [ sortDescriptor, setSortDescriptor ] = useState<SortDescriptor>(initialSortDescriptor);

    const sortedData = [ ...data ].sort((a, b) => {
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

export function ConfirmationModal({
    isOpen,
    onClose,
    onConfirm,
    isFetching,
    title,
    message,
    confirmButtonText = 'Confirm',
    cancelButtonText = 'Cancel',
    confirmButtonColor,
}: ConfirmationModalProps) {
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
    onButtonClick: () => void;
    className?: string;
    buttonClassName?: string;
};

export function EmptyState({
    message,
    buttonText,
    onButtonClick,
    buttonClassName = 'px-4 py-2',
}: EmptyStateProps) {
    return (
        <div className='text-center border p-6 rounded-lg border-default-200'>
            <p className='text-lg mb-4'>{message}</p>
            <Button
                className={buttonClassName}
                onClick={onButtonClick}
            >
                {buttonText}
            </Button>
        </div>
    );
}
