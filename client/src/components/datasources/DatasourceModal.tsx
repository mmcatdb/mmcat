import { Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Button, Input, useDisclosure, Select, SelectItem } from '@nextui-org/react';
import { useState } from 'react';
import { AddIcon } from '../icons/AddIcon';
import { api } from '@/api';
import {
    DATASOURCE_TYPES,
    DatasourceType,
    type Settings,
    validateSettings,
    type DatasourceInit,
} from '@/types/datasource';

export const DatasourceModal = () => {
    const { isOpen, onOpen, onOpenChange } = useDisclosure();
    const [ datasourceType, setDatasourceType ] = useState<DatasourceType | ''>('');
    const [ datasourceName, setDatasourceLabel ] = useState('');
    const [ settings, setSettings ] = useState<Settings>({});

    const handleSettingsChange = (field: keyof Settings, value: unknown) => {
        setSettings((prevSettings) => ({
            ...prevSettings,
            [field]: value,
        }));
    };

    const handleSubmit = async () => {
        if (!datasourceType || !validateSettings(settings, datasourceType)) {
            alert('Please fill out all required fields based on the datasource type.');
            return;
        }

        try {
            const newDatasource: DatasourceInit = {
                type: datasourceType,
                label: datasourceName,
                settings: settings,
            };

            // Call the API to create the datasource
            const createdDatasource = await api.datasources.createDatasource({}, newDatasource);

            console.log('Datasource created successfully:', createdDatasource);

            // Clear input fields
            setDatasourceType('');
            setDatasourceLabel('');
            setSettings({});

            // Close the modal
            onOpenChange();
        }
        catch (error) {
            console.error('Failed to create datasource:', error);
        }
    };

    const handleClose = () => {
        setDatasourceType('');
        setDatasourceLabel('');
        setSettings({});
        onOpenChange();
    };

    return (
        <>
            <Button onPress={onOpen} color='primary' startContent={<AddIcon />}>
                Add Datasource
            </Button>
            <Modal 
                isOpen={isOpen}
                onOpenChange={onOpenChange}
                isDismissable={false}
                isKeyboardDismissDisabled={true}
            >
                {/* TODO: Dark mode not working */}
                <ModalContent>
                    {() => (
                        <>
                            <ModalHeader className='flex flex-col gap-1'>Add Datasource</ModalHeader>
                            <ModalBody>
                                <Select
                                    items={DATASOURCE_TYPES}
                                    label='Type'
                                    placeholder='Select a Type'
                                    selectedKeys={datasourceType ? new Set([datasourceType]) : new Set()}
                                    onSelectionChange={(e) => {
                                        const selectedType = Array.from(e as Set<DatasourceType>)[0];
                                        setDatasourceType(selectedType);
                                    }}
                                >
                                    {(item) => (
                                        <SelectItem key={item.type}>
                                            {item.label}
                                        </SelectItem>
                                    )}
                                </Select>

                                <Input
                                    label='Datasource Label'
                                    value={datasourceName}
                                    onChange={(e) => setDatasourceLabel(e.target.value)}
                                    fullWidth
                                    required
                                />

                                {datasourceType && (
                                    <>
                                        {/* conditional input (based on selected datasource type) */}
                                        {[ 'mongodb', 'postgresql', 'neo4j' ].includes(datasourceType) && (
                                            <>
                                                <Input
                                                    label='Host'
                                                    value={settings.host || ''}
                                                    onChange={(e) => handleSettingsChange('host', e.target.value)}
                                                    fullWidth
                                                    required
                                                />
                                                <Input
                                                    label='Port'
                                                    value={settings.port || ''}
                                                    type="number"
                                                    onChange={(e) => handleSettingsChange('port', Number(e.target.value))}
                                                    fullWidth
                                                    required
                                                />
                                                <Input
                                                    label='Database'
                                                    value={settings.database || ''}
                                                    onChange={(e) => handleSettingsChange('database', e.target.value)}
                                                    fullWidth
                                                    required
                                                />
                                                <Input
                                                    label='Username'
                                                    value={settings.username || ''}
                                                    onChange={(e) => handleSettingsChange('username', e.target.value)}
                                                    fullWidth
                                                    required
                                                />
                                                <Input
                                                    label='Password'
                                                    type='password'
                                                    value={settings.password || ''}
                                                    onChange={(e) => handleSettingsChange('password', e.target.value)}
                                                    fullWidth
                                                    required
                                                />
                                                {datasourceType === DatasourceType.mongodb && (
                                                    <Input
                                                        label='Authentication Database'
                                                        value={settings.authenticationDatabase || ''}
                                                        onChange={(e) => handleSettingsChange('authenticationDatabase', e.target.value)}
                                                        fullWidth
                                                    />
                                                )}
                                            </>
                                        )}

                                        {[ 'csv', 'json', 'jsonld' ].includes(datasourceType) && (
                                            <Input
                                                label='File URL'
                                                value={settings.url || ''}
                                                onChange={(e) => handleSettingsChange('url', e.target.value)}
                                                fullWidth
                                                required
                                            />
                                        )}
                                    </>
                                )}
                            </ModalBody>
                            <ModalFooter>
                                <Button color='danger' variant='light' onPress={handleClose}>
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
};

export default DatasourceModal;
