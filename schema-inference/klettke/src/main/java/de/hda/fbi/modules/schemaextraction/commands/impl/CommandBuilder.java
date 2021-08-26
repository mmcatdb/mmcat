package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.Command;
import de.hda.fbi.modules.schemaextraction.commands.conditions.Condition;
import de.hda.fbi.modules.schemaextraction.common.DataType;

/**
 * @author daniel.mueller
 */
public class CommandBuilder {

    private CommandType _commandType;
    private String _sourceEntity;
    private String _sourceProperty;
    private String _targetEntity;
    private String _targetProperty;
    private DataType _dataType;
    private String _defaultValue;
    private boolean _darwinVersionProperty = false;
    private boolean _darwinIdProperty = false;
    private Integer _maxPropertyValue;
    private Integer _minPropertyValue;
    private boolean _required = false;
    private boolean _notNull = false;
    private Condition _condition = Condition.TRUE;

    public CommandBuilder() {
    }

    public Command build() {
        Command command = null;

        switch (_commandType) {
            case ADD_PROPERTY:
                AddPropertyCommand apc = new AddPropertyCommand(_sourceEntity, _sourceProperty);
                apc.setDataType(_dataType);
                if (_defaultValue != null) {
                    apc.setDefaultValue(_dataType.parse(_defaultValue, _dataType));
                }
                apc.setDarwin(_darwinVersionProperty);
                apc.setId(_darwinIdProperty);
                apc.setMax(_maxPropertyValue);
                apc.setMin(_minPropertyValue);
                apc.setRequired(_required);
                apc.setNullDisallowed(_notNull);

                command = apc;
                break;
            case COPY_PROPERTY:

                CopyPropertyCommand cpc = null;
                if (this._targetProperty != null) {
                    cpc = new CopyPropertyCommand(_sourceEntity, _sourceProperty, _targetEntity, _targetProperty);
                } else {
                    cpc = new CopyPropertyCommand(_sourceEntity, _sourceProperty, _targetEntity);
                }
                cpc.setCondition(_condition);
                command = cpc;
                break;
            case CREATE_TYPE:
                command = new CreateEntityTypeCommand(_sourceEntity);
                break;

            case MOVE_PROPERTY:
                MovePropertyCommand mpc;
                if (this._targetProperty != null) {
                    mpc = new MovePropertyCommand(_sourceEntity, _sourceProperty, _targetEntity, _targetProperty);
                } else {
                    mpc = new MovePropertyCommand(_sourceEntity, _sourceProperty, _targetEntity);
                }

                mpc.setCondition(_condition);
                command = mpc;
                break;
            case DELETE_PROPERTY:
                DeletePropertyCommand dpc = new DeletePropertyCommand(_sourceEntity, _sourceProperty);

                command = dpc;
                break;
            case RENAME_PROPERTY:

                RenamePropertyCommand rpc = new RenamePropertyCommand(_sourceEntity, _sourceProperty, _targetProperty);

                command = rpc;
                break;
            default:
                throw new IllegalStateException("No command type specified.");
        }

        this.setConditionBackToDefault();

        return command;

    }

    public CommandBuilder createEntity(String entityName) {
        _commandType = CommandType.CREATE_TYPE;

        _sourceEntity = entityName;
        return this;
    }

    public CommandBuilder dropEntity(String entityName) {
        _commandType = CommandType.DROP_TYPE;
        _sourceEntity = entityName;
        return this;
    }

    public CommandBuilder addProperty() {
        _commandType = CommandType.ADD_PROPERTY;
        return this;
    }

    public CommandBuilder renameProperty() {
        _commandType = CommandType.RENAME_PROPERTY;
        return this;
    }

    public CommandBuilder deleteProperty() {
        _commandType = CommandType.DELETE_PROPERTY;
        return this;
    }

    public CommandBuilder moveProperty() {
        _commandType = CommandType.MOVE_PROPERTY;
        return this;
    }

    public CommandBuilder copyProperty() {
        _commandType = CommandType.COPY_PROPERTY;
        return this;
    }

    public CommandBuilder from(String propertyName, String entityName) {
        _sourceEntity = entityName;
        _sourceProperty = propertyName;
        return this;
    }

    public CommandBuilder to(String propertyName, String entityName) {
        _targetEntity = entityName;
        _targetProperty = propertyName;
        return this;
    }

    public CommandBuilder toProperty(String propertyName) {
        _targetProperty = propertyName;
        return this;
    }

    public CommandBuilder toEntity(String entityName) {
        _targetEntity = entityName;
        return this;
    }

    public CommandBuilder where(Condition condition) {
        _condition = condition;
        return this;
    }

    public CommandBuilder defaultValue(String value) {
        _defaultValue = value;
        return this;
    }

    public CommandBuilder dataType(DataType dataType) {
        _dataType = dataType;
        return this;
    }

    public CommandBuilder describeEntity(String entityTypeName) {
        _commandType = CommandType.DESCRIBE_TYPE;
        _sourceEntity = entityTypeName;
        return this;
    }

    public CommandBuilder showEntity(String entityTypeName) {
        _commandType = CommandType.SHOW_TYPE;
        _sourceEntity = entityTypeName;
        return this;
    }

    public CommandBuilder renameEntity(String entityTypeName) {
        _commandType = CommandType.RENAME_TYPE;
        _sourceEntity = entityTypeName;
        return this;
    }

    public CommandBuilder maxPropertyValue(int value) {
        _maxPropertyValue = value;
        return this;
    }

    public CommandBuilder minPropertyValue(int value) {
        _minPropertyValue = value;
        return this;
    }

    public CommandBuilder id(boolean isId) {
        _darwinIdProperty = isId;
        return this;
    }

    public CommandBuilder version(boolean isVersion) {
        _darwinVersionProperty = isVersion;
        return this;
    }

    public CommandBuilder required(boolean required) {
        _required = required;
        return this;
    }

    public CommandBuilder notNull(boolean notNull) {
        _notNull = notNull;
        return this;
    }

    // public CommandBuilder dataType(Class<?> dataType) {
    // _dataType = dataType;
    // return this;
    // }

    private void setConditionBackToDefault() {
        this._condition = Condition.TRUE;
    }

    public enum CommandType {
        CREATE_TYPE, DESCRIBE_TYPE, SHOW_TYPE, DROP_TYPE, RENAME_TYPE, ADD_PROPERTY, RENAME_PROPERTY, DELETE_PROPERTY, MOVE_PROPERTY, COPY_PROPERTY
    }
}
