package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.SimpleCommand;
import de.hda.fbi.modules.schemaextraction.commands.conditions.Condition;

/**
 * @author daniel.mueller
 */
@SuppressWarnings("serial")
public abstract class AbstractSimpleCommand implements SimpleCommand {

    protected String _entityType;
    protected String _property;
    private Condition _condition;
    private int _version;

    protected AbstractSimpleCommand() {
    }

    public AbstractSimpleCommand(String entityTypeName) {
        _entityType = entityTypeName;
    }

    public AbstractSimpleCommand(String entityTypeName, String propertyName) {
        this(entityTypeName);
        _property = propertyName;
    }

    @Override
    public String getEntityType() {
        return _entityType;
    }

    public void setEntityType(String entityType) {
        _entityType = entityType;
    }

    public String getPropertyName() {
        return _property;
    }

    public void setProperty(String property) {
        _property = property;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCondition() == null) ? 0 : getCondition().hashCode());
        result = prime * result + ((_entityType == null) ? 0 : _entityType.hashCode());
        result = prime * result + ((_property == null) ? 0 : _property.hashCode());
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
        AbstractSimpleCommand other = (AbstractSimpleCommand) obj;
        if (getCondition() == null) {
            if (other.getCondition() != null) {
                return false;
            }
        } else if (!getCondition().equals(other.getCondition())) {
            return false;
        }
        if (_entityType == null) {
            if (other._entityType != null) {
                return false;
            }
        } else if (!_entityType.equals(other._entityType)) {
            return false;
        }
        if (_property == null) {
            return other._property == null;
        } else {
            return _property.equals(other._property);
        }
    }

    @Override
    public Condition getCondition() {
        return _condition;
    }

    public void setCondition(Condition _condition) {
        this._condition = _condition;
    }

    @Override
    public boolean isAddPropertyCommand() {
        return this instanceof AddPropertyCommand;
    }

    @Override
    public boolean isCopyPropertyCommand() {
        return false;
    }

    @Override
    public boolean isCreateEntityTypeCommand() {
        return this instanceof CreateEntityTypeCommand;
    }

    @Override
    public boolean isMovePropertyCommand() {
        return false;
    }

    @Override
    public boolean isRenamePropertyCommand() {
        return this instanceof RenamePropertyCommand;
    }

    @Override
    public boolean isDeletePropertyCommand() {
        return this instanceof DeletePropertyCommand;
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
