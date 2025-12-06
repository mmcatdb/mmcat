import { Button, Modal, ModalBody, ModalContent, ModalFooter, ModalHeader } from '@heroui/react';
import { type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { cn } from '@/components/common/utils';
import { SpinnerButton } from './components';

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
                    <SpinnerButton color={confirmButtonColor} onPress={onConfirm} isFetching={isFetching}>
                        {confirmButtonText}
                    </SpinnerButton>
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}

type EmptyStateProps = {
    message: string;
    buttonText: string;
    className?: string;
    buttonStartContent?: ReactNode;
    buttonClassName?: string;
} & ({
    to: string;
} | {
    onClick: () => void;
});

/**
 * A placeholder for empty states in tables.
 */
export function EmptyState({ message, buttonText, buttonClassName, buttonStartContent, ...action }: EmptyStateProps) {
    return (
        <div className='text-center border-2 border-dashed border-default-200 p-12 rounded-xl'>
            <p className='text-lg mb-4'>{message}</p>

            {'to' in action ? (
                <Button as={Link} to={action.to} className={cn('px-4 py-2', buttonClassName)} startContent={buttonStartContent}>
                    {buttonText}
                </Button>
            ) : (
                <span>
                    <Button onPress={action.onClick} className={cn('px-4 py-2', buttonClassName)} startContent={buttonStartContent}>
                        {buttonText}
                    </Button>
                </span>
            )}
        </div>
    );
}
