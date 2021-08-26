package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.ComplexCommand;
import de.hda.fbi.modules.schemaextraction.commands.conditions.Condition;

/**
 * @author daniel.mueller
 */
@SuppressWarnings("serial")
public abstract class AbstractComplexCommand implements ComplexCommand {

    protected String _sourceEntityType;
    protected String _sourceProperty;
    protected String _targetEntityType;
    protected String _targetProperty;
    protected Condition _condition;
    private int _version;

    protected AbstractComplexCommand() {
    }

    public AbstractComplexCommand(String sourceEntityType, String sourceProperty) {
        _sourceEntityType = sourceEntityType;
        _sourceProperty = sourceProperty;
    }

    @Override
    public String getEntityType() {
        return _sourceEntityType;
    }

    @Override
    public Condition getCondition() {
        return _condition;
    }

    public void setCondition(Condition condition) {
        _condition = condition;
    }

    @Override
    public String getTargetEntityType() {
        return _targetEntityType;
    }

    protected void setTargetEntityType(String entityType) {
        _targetEntityType = entityType;
    }

    @Override
    public String getTargetProperty() {
        return _targetProperty;
    }

    protected void setTargetProperty(String propertyName) {
        _targetProperty = propertyName;
    }

    @Override
    public String getPropertyName() {
        return _sourceProperty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_condition == null) ? 0 : _condition.hashCode());
        result = prime * result + ((_sourceEntityType == null) ? 0 : _sourceEntityType.hashCode());
        result = prime * result + ((_sourceProperty == null) ? 0 : _sourceProperty.hashCode());
        result = prime * result + ((_targetEntityType == null) ? 0 : _targetEntityType.hashCode());
        result = prime * result + ((_targetProperty == null) ? 0 : _targetProperty.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractComplexCommand other = (AbstractComplexCommand) obj;
        if (_condition == null) {
            if (other._condition != null) {
                return false;
            }
        } else if (!_condition.equals(other._condition)) {
            return false;
        }
        if (_sourceEntityType == null) {
            if (other._sourceEntityType != null) {
                return false;
            }
        } else if (!_sourceEntityType.equals(other._sourceEntityType)) {
            return false;
        }
        if (_sourceProperty == null) {
            if (other._sourceProperty != null) {
                return false;
            }
        } else if (!_sourceProperty.equals(other._sourceProperty)) {
            return false;
        }
        if (_targetEntityType == null) {
            if (other._targetEntityType != null) {
                return false;
            }
        } else if (!_targetEntityType.equals(other._targetEntityType)) {
            return false;
        }
        if (_targetProperty == null) {
            return other._targetProperty == null;
        } else {
            return _targetProperty.equals(other._targetProperty);
        }
    }

    @Override
    public boolean isAddPropertyCommand() {
        return false;
    }

    @Override
    public boolean isCopyPropertyCommand() {
        return this instanceof CopyPropertyCommand;
    }

    @Override
    public boolean isCreateEntityTypeCommand() {
        return false;
    }

    @Override
    public boolean isMovePropertyCommand() {
        return this instanceof MovePropertyCommand;
    }

    @Override
    public boolean isRenamePropertyCommand() {
        return false;
    }

    @Override
    public boolean isDeletePropertyCommand() {
        return false;
    }

    @Override
    public int getCommandVersion() {
        return _version;
    }

    @Override
    public void setCommandVersion(int version) {
        _version = version;
    }

}
