import { Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Button, Input, Select, SelectItem } from '@nextui-org/react';
import { useEffect, useState } from 'react';
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
    isOpen: boolean;
    onClose: () => void;
    onDatasourceCreated: (newDatasource: Datasource) => void;
};

export const DatasourceModal = ({ 
    isOpen, 
    onClose, 
    onDatasourceCreated,
}: DatasourceModalProps) => {
    const [ datasourceType, setDatasourceType ] = useState<DatasourceType | ''>('');
    const [ datasourceName, setDatasourceLabel ] = useState('');
    const [ settings, setSettings ] = useState<Settings>({});
    const [ isCreatingDatasource, setIsCreatingDatasource] = useState<boolean>(false);

    const resetForm = () => {
        setDatasourceType('');
        setDatasourceLabel('');
        setSettings({});
    };

    useEffect(() => {
        if (!isOpen) 
            resetForm();
    }, [ isOpen ]);

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
            setIsCreatingDatasource(true);
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
                onClose();
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
        finally {
            setIsCreatingDatasource(false);
        }
    };

    return (
        <Modal 
            isOpen={isOpen}
            onClose={onClose}
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
                                isDisabled={isCreatingDatasource}
                            />

                            <Input
                                label='Datasource Label'
                                value={datasourceName}
                                onChange={(e) => setDatasourceLabel(e.target.value)}
                                fullWidth
                                required
                                isDisabled={isCreatingDatasource}
                            />

                            {datasourceType && (
                                <>
                                    {/* conditional input (based on selected datasource type) */}
                                    {[ 'mongodb', 'postgresql', 'neo4j' ].includes(datasourceType) && (
                                        <>
                                            <Input
                                                label='Host'
                                                value={settings.host ?? ''}
                                                onChange={(e) => handleSettingsChange('host', e.target.value)}
                                                fullWidth
                                                required
                                                isDisabled={isCreatingDatasource}
                                            />
                                            <Input
                                                label='Port'
                                                value={settings.port != null ? String(settings.port) : ''}
                                                type='number'
                                                onChange={(e) => handleSettingsChange('port', Number(e.target.value))}
                                                fullWidth
                                                required
                                                isDisabled={isCreatingDatasource}
                                            />
                                            <Input
                                                label='Database'
                                                value={settings.database ?? ''}
                                                onChange={(e) => handleSettingsChange('database', e.target.value)}
                                                fullWidth
                                                required
                                                isDisabled={isCreatingDatasource}
                                            />
                                            <Input
                                                label='Username'
                                                value={settings.username ?? ''}
                                                onChange={(e) => handleSettingsChange('username', e.target.value)}
                                                fullWidth
                                                required
                                                isDisabled={isCreatingDatasource}
                                            />
                                            <Input
                                                label='Password'
                                                type='password'
                                                value={settings.password ?? ''}
                                                onChange={(e) => handleSettingsChange('password', e.target.value)}
                                                fullWidth
                                                required
                                                isDisabled={isCreatingDatasource}
                                            />
                                            {datasourceType === DatasourceType.mongodb && (
                                                <Input
                                                    label='Authentication Database'
                                                    value={settings.authenticationDatabase ?? ''}
                                                    onChange={(e) => handleSettingsChange('authenticationDatabase', e.target.value)}
                                                    fullWidth
                                                    required
                                                    isDisabled={isCreatingDatasource}
                                                />
                                            )}
                                        </>
                                    )}

                                    {[ 'csv', 'json', 'jsonld' ].includes(datasourceType) && (
                                        <Input
                                            label='File URL'
                                            value={settings.url ?? ''}
                                            onChange={(e) => handleSettingsChange('url', e.target.value)}
                                            fullWidth
                                            required
                                            isDisabled={isCreatingDatasource}
                                        />
                                    )}
                                </>
                            )}
                        </ModalBody>
                        <ModalFooter>
                            <Button
                                color='danger'
                                variant='light'
                                onPress={onClose}
                                isDisabled={isCreatingDatasource}
                            >
                                    Close
                            </Button>
                            <Button
                                color='primary'
                                onPress={handleSubmit}
                                isLoading={isCreatingDatasource}
                            >
                                    Submit
                            </Button>
                        </ModalFooter>
                    </>
                )}
            </ModalContent>
        </Modal>

    );
};

type SelectDatasourceTypeProps = {
    datasourceType: DatasourceType | '';
    setDatasourceType: (type: DatasourceType) => void;
    isDisabled: boolean;
};

const SelectDatasourceType = ({ datasourceType, setDatasourceType, isDisabled }: SelectDatasourceTypeProps) => (
    <Select
        items={DATASOURCE_TYPES}
        label='Type'
        placeholder='Select a Type'
        selectedKeys={datasourceType ? new Set([ datasourceType ]) : new Set()}
        onSelectionChange={(e) => {
            const selectedType = Array.from(e as Set<DatasourceType>)[0];
            setDatasourceType(selectedType);
        }}
        isDisabled={isDisabled}
    >
        {(item) => (
            <SelectItem key={item.type}>
                {item.label}
            </SelectItem>
        )}
    </Select>
);
