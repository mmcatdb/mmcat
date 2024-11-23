import { Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Button, Input, useDisclosure, Select, SelectItem } from '@nextui-org/react';
import { useState } from 'react';
import { AddIcon } from '@/components/icons/PlusIcon';
import { api } from '@/api';
import {
    DATASOURCE_TYPES,
    DatasourceType,
    type Settings,
    validateSettings,
    type DatasourceInit,
    type Datasource,
} from '@/types/datasource';
import { toast } from 'react-toastify';

type DatasourceModalProps = {
    onDatasourceCreated: (newDatasource: Datasource) => void;
};

export const DatasourceModal = ({ onDatasourceCreated }: DatasourceModalProps) => {
    const { isOpen, onOpen, onOpenChange } = useDisclosure();
    const [ datasourceType, setDatasourceType ] = useState<DatasourceType | ''>('');
    const [ datasourceName, setDatasourceLabel ] = useState('');
    const [ settings, setSettings ] = useState<Settings>({});

    const resetForm = () => {
        setDatasourceType('');
        setDatasourceLabel('');
        setSettings({});
    };

    const handleSettingsChange = (field: keyof Settings, value: unknown) => {
        setSettings((prevSettings) => ({
            ...prevSettings,
            [field]: value,
        }));
    };

    const handleSubmit = async () => {
        if (!datasourceType || !validateSettings(settings, datasourceType)) {
            toast.error('Please fill out all fields.');
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

            if (createdDatasource.status && createdDatasource.data) {
                // Notify parent
                onDatasourceCreated(createdDatasource.data);
                resetForm();
                onOpenChange();
                toast.success('Datasource created.');
            }
            else {
                toast.error('Failed to create datasource. Please try again.');
            }
        }
        catch (error) {
            console.error('An unexpected error occurred:', error);
            toast.error('An unexpected error occurred. Please try again.');
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
                hideCloseButton
            >
                <ModalContent>
                    {() => (
                        <>
                            <ModalHeader className='flex flex-col gap-1'>Add Datasource</ModalHeader>
                            <ModalBody>
                                <SelectDatasourceType 
                                    datasourceType={datasourceType}
                                    setDatasourceType={setDatasourceType}
                                />

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

type SelectDatasourceTypeProps = {
    datasourceType: DatasourceType | '';
    setDatasourceType: (type: DatasourceType) => void;
};

const SelectDatasourceType = ({ datasourceType, setDatasourceType }: SelectDatasourceTypeProps) => (
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
);
