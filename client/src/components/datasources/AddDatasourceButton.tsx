import { Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Button, useDisclosure } from '@nextui-org/react';
import { AddIcon } from '../icons/AddIcon';

export default function App() {
    const { isOpen, onOpen, onOpenChange } = useDisclosure();

    return (
        <>
            <Button onPress={onOpen} color='primary' startContent={<AddIcon />}>Add Datasource</Button>
            <Modal isOpen={isOpen} onOpenChange={onOpenChange} isDismissable={false} isKeyboardDismissDisabled={true}>
                <ModalContent>
                    {(onClose) => (
                        <>
                            <ModalHeader className='flex flex-col gap-1'>Add Datasource</ModalHeader>
                            <ModalBody>
                                <p> 
                                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                                    Nullam pulvinar risus non risus hendrerit venenatis.
                                    Pellentesque sit amet hendrerit risus, sed porttitor quam.
                                </p>
                                {/* TODO: add Form component here */}
                                
                            </ModalBody>
                            <ModalFooter>
                                <Button color='danger' variant='light' onPress={onClose}>
                                    Close
                                </Button>
                                <Button color='primary' onPress={onClose}>
                                    Action
                                </Button>
                            </ModalFooter>
                        </>
                    )}
                </ModalContent>
            </Modal>
        </>
    );
}
