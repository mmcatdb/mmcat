import { Modal, ModalContent, ModalHeader, ModalBody, ModalFooter, Button, Input, Select, SelectItem, Checkbox } from '@heroui/react';
import { useEffect, useState, useCallback } from 'react';
import { api } from '@/api';
import { Datasource, DatasourceType, type DatasourceSettings, validateSettings, type DatasourceInit, DATASOURCE_TYPES } from '@/types/Datasource';
import { toast } from 'react-toastify';

type DatasourceModalProps = {
    /** Whether the modal is visible. */
    isOpen: boolean;
    /** Callback to close the modal. */
    onClose: () => void;
    /** Callback to handle successful datasource creation. */
    onDatasourceCreated: (newDatasource: Datasource) => void;
};

type SelectDatasourceTypeProps = {
    /** The currently selected datasource type. */
    datasourceType: DatasourceType | '';
    /** Function to update the datasource type and settings. */
    setDatasourceType: (type: DatasourceType, prevSettings: DatasourceSettings) => void;
};

type DatasourceSpecificFieldsProps = {
    /** The selected datasource type. */
    datasourceType: DatasourceType;
    /** The current settings for the datasource. */
    settings: DatasourceSettings;
    /** Function to update settings fields. */
    handleSettingsChange: (field: keyof DatasourceSettings, value: unknown) => void;
};

type FormButtonsProps = {
    onSubmit: () => void;
    onCancel: () => void;
    isSubmitting: boolean;
};

/**
 * Hook to manage datasource form state and submission.
 *
 * @param onClose - Callback to close the modal.
 * @param onDatasourceCreated - Callback to handle successful creation.
 * @returns Form state and handlers.
 */
function useDatasourceForm(onClose: () => void, onDatasourceCreated: (newDatasource: Datasource) => void) {
    const [ datasourceType, setDatasourceType ] = useState<DatasourceType | ''>('');
    const [ datasourceName, setDatasourceLabel ] = useState('');
    const [ settings, setSettings ] = useState<DatasourceSettings>({});
    const [ isCreatingDatasource, setIsCreatingDatasource ] = useState<boolean>(false);

    /**
     * Resets the form to its initial state.
     */
    const resetForm = useCallback(() => {
        setDatasourceType('');
        setDatasourceLabel('');
        setSettings({});
    }, []);

    /**
     * Updates a settings field.
     */
    const handleSettingsChange = useCallback((field: keyof DatasourceSettings, value: unknown) => {
        setSettings(prevSettings => ({
            ...prevSettings,
            [field]: value,
        }));
    }, []);

    /**
     * Updates the datasource type and initializes settings.
     */
    const handleSetDatasourceType = useCallback(
        (type: DatasourceType, prevSettings: DatasourceSettings) => {
            setDatasourceType(type);
            setSettings(initializeSettings(type, prevSettings));
        },
        [],
    );

    /**
     * Submits the form to create a new datasource.
     */
    const handleSubmit = useCallback(async () => {
        if (!datasourceType || !validateSettings(settings, datasourceType)) {
            toast.error('Please fill out all fields.');
            return;
        }

        setIsCreatingDatasource(true);
        const newDatasource: DatasourceInit = {
            type: datasourceType,
            label: datasourceName,
            settings,
        };

        // Call the API to create the datasource
        const createdDatasource = await api.datasources.createDatasource({}, newDatasource);

        if (createdDatasource.status && createdDatasource.data) {
            onDatasourceCreated(Datasource.fromResponse(createdDatasource.data));
            resetForm();
            onClose();
            toast.success('Datasource created.');
        }
        else {
            toast.error('Failed to create datasource. Please try again.');
        }
        setIsCreatingDatasource(false);
    }, [ datasourceType, datasourceName, settings, onDatasourceCreated, resetForm, onClose ]);

    return {
        datasourceType,
        datasourceName,
        settings,
        isCreatingDatasource,
        setDatasourceLabel,
        handleSettingsChange,
        handleSetDatasourceType,
        handleSubmit,
        resetForm,
    };
}

/**
 * Initializes settings for a given datasource type.
 *
 * @param type - The selected datasource type.
 * @param currentSettings - The current settings to merge with defaults.
 */
function initializeSettings(type: DatasourceType, currentSettings: DatasourceSettings): DatasourceSettings {
    const isDatabaseType = [ DatasourceType.mongodb, DatasourceType.postgresql, DatasourceType.neo4j ].includes(type);
    return {
        ...currentSettings,
        isWritable: isDatabaseType,
        isQueryable: isDatabaseType,
    };
}

/**
 * Reusable component for modal submit and cancel buttons.
 */
function FormButtons({ onSubmit, onCancel, isSubmitting }: FormButtonsProps) {
    return (<>
        <Button color='danger' variant='light' onPress={onCancel} isDisabled={isSubmitting}>
                Close
        </Button>
        <Button color='primary' onPress={onSubmit} isLoading={isSubmitting}>
                Submit
        </Button>
    </>);
}

/**
 * Renders a modal for creating a new datasource with type-specific fields.
 */
export function DatasourceModal({
    isOpen,
    onClose,
    onDatasourceCreated,
}: DatasourceModalProps) {
    const {
        datasourceType,
        datasourceName,
        settings,
        isCreatingDatasource,
        setDatasourceLabel,
        handleSettingsChange,
        handleSetDatasourceType,
        handleSubmit,
        resetForm,
    } = useDatasourceForm(onClose, onDatasourceCreated);

    // Reset form when modal closes
    useEffect(() => {
        if (!isOpen)
            resetForm();

    }, [ isOpen, resetForm ]);

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
                        setDatasourceType={handleSetDatasourceType}
                    />

                    <Input
                        label='Datasource Label'
                        value={datasourceName}
                        onChange={e => setDatasourceLabel(e.target.value)}
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
                    <FormButtons
                        onSubmit={handleSubmit}
                        onCancel={onClose}
                        isSubmitting={isCreatingDatasource}
                    />
                </ModalFooter>
            </ModalContent>
        </Modal>
    );
}

/**
 * Renders a dropdown to select the datasource type.
 */
function SelectDatasourceType({ datasourceType, setDatasourceType }: SelectDatasourceTypeProps) {
    return (
        <Select
            items={datasourceOptions}
            label='Type'
            placeholder='Select a Type'
            selectedKeys={datasourceType ? new Set([ datasourceType ]) : new Set()}
            onSelectionChange={e => {
                const selectedType = Array.from(e as Set<DatasourceType>)[0];
                if (selectedType)
                    setDatasourceType(selectedType, {});
            }}
        >
            {item => (
                <SelectItem key={item.value}>
                    {item.label}
                </SelectItem>
            )}
        </Select>
    );
}

const datasourceOptions = Object.values(DATASOURCE_TYPES).map(def => ({
    value: def.type,
    label: def.label,
}));

/**
 * Renders type-specific fields for configuring the datasource.
 */
export function DatasourceSpecificFields({ datasourceType, settings, handleSettingsChange }: DatasourceSpecificFieldsProps) {
    if ([ DatasourceType.mongodb, DatasourceType.postgresql, DatasourceType.neo4j ].includes(datasourceType)) {
        return (<>
            <Input
                label='Host'
                value={settings.host ?? ''}
                onChange={e => handleSettingsChange('host', e.target.value)}
                fullWidth
                required
            />
            <Input
                label='Port'
                value={settings.port != null ? String(settings.port) : ''}
                type='number'
                onChange={e => handleSettingsChange('port', Number(e.target.value))}
                fullWidth
                required
            />
            <Input
                label='Database'
                value={settings.database ?? ''}
                onChange={e => handleSettingsChange('database', e.target.value)}
                fullWidth
                required
            />
            <Input
                label='Username'
                value={settings.username ?? ''}
                onChange={e => handleSettingsChange('username', e.target.value)}
                fullWidth
                required
            />
            <Input
                label='Password'
                type='password'
                value={settings.password ?? ''}
                onChange={e => handleSettingsChange('password', e.target.value)}
                fullWidth
                required
            />
            {datasourceType === DatasourceType.mongodb && (
                <Input
                    label='Authentication Database'
                    value={settings.authenticationDatabase ?? ''}
                    onChange={e => handleSettingsChange('authenticationDatabase', e.target.value)}
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
        </>);
    }

    if ([ DatasourceType.csv, DatasourceType.json, DatasourceType.jsonld ].includes(datasourceType)) {
        return (<>
            <Input
                label='File URL'
                value={settings.url ?? ''}
                onChange={e => handleSettingsChange('url', e.target.value)}
                fullWidth
                required
            />
            {datasourceType === DatasourceType.csv && (<>
                <Input
                    label='Separator'
                    value={settings.separator ?? ''}
                    maxLength={1}
                    onChange={e => handleSettingsChange('separator', e.target.value)}
                    fullWidth
                    required
                />
                <Checkbox
                    isSelected={settings.hasHeader ?? false}
                    onChange={() => handleSettingsChange('hasHeader', !(settings.hasHeader ?? false))}
                >
                        Has Header?
                </Checkbox>
            </>)}
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
        </>);
    }

    return null;
}
