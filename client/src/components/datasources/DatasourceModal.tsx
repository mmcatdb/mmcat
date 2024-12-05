import { Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Button, Input, Select, SelectItem, Checkbox } from '@nextui-org/react';
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

export function DatasourceModal({ 
    isOpen, 
    onClose, 
    onDatasourceCreated,
}: DatasourceModalProps) {
    const [ datasourceType, setDatasourceType ] = useState<DatasourceType | ''>('');
    const [ datasourceName, setDatasourceLabel ] = useState('');
    const [ settings, setSettings ] = useState<Settings>({});
    const [ isCreatingDatasource, setIsCreatingDatasource ] = useState<boolean>(false);

    function resetForm() {
        setDatasourceType('');
        setDatasourceLabel('');
        setSettings({});
    }

    useEffect(() => {
        if (!isOpen) 
            resetForm();
    }, [ isOpen ]);

    // console.log(settings);
    function handleSettingsChange(field: keyof Settings, value: unknown) {
        setSettings((prevSettings) => ({
            ...prevSettings,
            [field]: value,
        }));
    }

    async function handleSubmit() {
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
    }

    return (
        <Modal 
            isOpen={isOpen}
            onClose={onClose}
            isDismissable={false}
            isKeyboardDismissDisabled={true}
            hideCloseButton
        >
            <ModalContent>
                <ModalHeader className='flex flex-col gap-1'>Add Datasource</ModalHeader>
                <ModalBody>
                    <SelectDatasourceType
                        datasourceType={datasourceType}
                        // TODO: selectPredefinedSettings, updateDatasourceType
                        // TODO: do this also for isQueryable
                        setDatasourceType={t => setDatasourceType(t) || setSettings(s => ({ ...s, isWritable: true }))}
                    />

                    <Input
                        label='Datasource Label'
                        value={datasourceName}
                        onChange={(e) => setDatasourceLabel(e.target.value)}
                        fullWidth
                        required
                    />

                    {datasourceType && (
                        <DatasourceSpecificFields
                            datasourceType={datasourceType}
                            settings={settings}
                            handleSettingsChange={handleSettingsChange}
                        />
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
            </ModalContent>
        </Modal>

    );
}

type SelectDatasourceTypeProps = {
    datasourceType: DatasourceType | '';
    setDatasourceType: (type: DatasourceType) => void;
};

function SelectDatasourceType({ datasourceType, setDatasourceType }: SelectDatasourceTypeProps) {
    return (
        <Select
            items={DATASOURCE_TYPES}
            label='Type'
            placeholder='Select a Type'
            selectedKeys={datasourceType ? new Set([ datasourceType ]) : new Set()}
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
}

type DatasourceSpecificFieldsProps = {
    datasourceType: DatasourceType;
    settings: Settings;
    handleSettingsChange: (field: keyof Settings, value: unknown) => void;
};

export function DatasourceSpecificFields({ datasourceType, settings, handleSettingsChange }: DatasourceSpecificFieldsProps) {
    if ([ 'mongodb', 'postgresql', 'neo4j' ].includes(datasourceType)) {
        return (
            <>
                <Input
                    label='Host'
                    value={settings.host ?? ''}
                    onChange={(e) => handleSettingsChange('host', e.target.value)}
                    fullWidth
                    required
                />
                <Input
                    label='Port'
                    value={settings.port != null ? String(settings.port) : ''}
                    type='number'
                    onChange={(e) => handleSettingsChange('port', Number(e.target.value))}
                    fullWidth
                    required
                />
                <Input
                    label='Database'
                    value={settings.database ?? ''}
                    onChange={(e) => handleSettingsChange('database', e.target.value)}
                    fullWidth
                    required
                />
                <Input
                    label='Username'
                    value={settings.username ?? ''}
                    onChange={(e) => handleSettingsChange('username', e.target.value)}
                    fullWidth
                    required
                />
                <Input
                    label='Password'
                    type='password'
                    value={settings.password ?? ''}
                    onChange={(e) => handleSettingsChange('password', e.target.value)}
                    fullWidth
                    required
                />
                {datasourceType === DatasourceType.mongodb && (
                    <Input
                        label='Authentication Database'
                        value={settings.authenticationDatabase ?? ''}
                        onChange={(e) => handleSettingsChange('authenticationDatabase', e.target.value)}
                        fullWidth
                        required
                    />
                )}
                <Checkbox
                    isSelected={settings.isWritable ?? false}
                    onChange={() => handleSettingsChange('isWritable', !(settings.isWritable ?? false))}
                >
                    Is Writable?
                </Checkbox>

                <Checkbox
                    isSelected={settings.isQueryable ?? false}
                    onChange={() => handleSettingsChange('isQueryable', !(settings.isQueryable ?? false))}
                >
                    Is Queryable?
                </Checkbox>
            </>
        );
    }

    if ([ 'csv', 'json', 'jsonld' ].includes(datasourceType)) {
        return (
            <>
                <Input
                    label='File URL'
                    value={settings.url ?? ''}
                    onChange={(e) => handleSettingsChange('url', e.target.value)}
                    fullWidth
                    required
                />
                {[ 'csv' ].includes(datasourceType) && (
                    <>
                        <Input
                            label='Separator'
                            value={settings.separator ?? ''}
                            // Has to be one char
                            maxLength={1}
                            onChange={(e) => handleSettingsChange('separator', e.target.value)}
                            fullWidth
                            required
                        />
                        <Checkbox
                            isSelected={settings.hasHeader ?? false}
                            onChange={() => handleSettingsChange('hasHeader', !(settings.hasHeader ?? false))}
                        >
                            Has Header?
                        </Checkbox>
                    </>
                )}
                <Checkbox
                    isSelected={settings.isWritable ?? false}
                    onChange={() => handleSettingsChange('isWritable', !(settings.isWritable ?? false))}
                >
                    Is Writable?
                </Checkbox>

                <Checkbox
                    isSelected={settings.isQueryable ?? false}
                    onChange={() => handleSettingsChange('isQueryable', !(settings.isQueryable ?? false))}
                >
                    Is Queryable?
                </Checkbox>
            </>
            
        );
    }

    return null;
}
