import { Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Button, Input, useDisclosure, Select, SelectItem } from '@nextui-org/react';
import { useState } from 'react';
import { AddIcon } from '../icons/AddIcon';
import { animals } from './datasourceTypes';

export default function App() {
    const { isOpen, onOpen, onOpenChange } = useDisclosure();
    const [ datasourceType, setDatasourceType ] = useState('');
    const [ datasourceName, setDatasourceLabel ] = useState('');
    const [ datasourceSettings, setDatasourceSettings ] = useState('');
    const [ datasourceConfig, setDatasourceConfig ] = useState('');

    const handleSubmit = () => {
        // TODO: form submission handling
        console.log({
            datasourceName,
            datasourceType,
            datasourceSettings,
            datasourceConfig,
        });
    };

    return (
        <>
            <Button onPress={onOpen} color='primary' startContent={<AddIcon />}>Add Datasource</Button>
            <Modal isOpen={isOpen} onOpenChange={onOpenChange} isDismissable={false} isKeyboardDismissDisabled={true}>
                {/* TODO: Dark mode not working */}
                <ModalContent>
                    {(onClose) => (
                        <>
                            <ModalHeader className='flex flex-col gap-1'>Add Datasource</ModalHeader>
                            <ModalBody>
                                <Select
                                    items={animals}
                                    label='Type'
                                    placeholder='Select a Type'
                                    onChange={(e) => setDatasourceType(e.target.value)}
                                    className='max-w-xs'
                                >
                                    {(animal) => <SelectItem key={animal.key}>{animal.label}</SelectItem>}
                                </Select>

                                <Input
                                    label='Datasource Label'
                                    placeholder='Enter datasource label'
                                    value={datasourceName}
                                    onChange={(e) => setDatasourceLabel(e.target.value)}
                                    fullWidth
                                    required
                                />

                                <Input
                                    label='Settings'
                                    placeholder='Enter datasource settings'
                                    value={datasourceSettings}
                                    onChange={(e) => setDatasourceSettings(e.target.value)}
                                    fullWidth
                                    required
                                />

                                <Input
                                    label='Configuration'
                                    placeholder='Enter datasource configuration'
                                    value={datasourceConfig}
                                    onChange={(e) => setDatasourceConfig(e.target.value)}
                                    fullWidth
                                    required
                                />
                            </ModalBody>
                            <ModalFooter>
                                <Button color='danger' variant='light' onPress={onClose}>
                                    Close
                                </Button>
                                <Button color='primary' onPress={handleSubmit}>
                                    Submit
                                </Button>
                            </ModalFooter>
                        </>
                    )}
                </ModalContent>
            </Modal>
        </>
    );
}
