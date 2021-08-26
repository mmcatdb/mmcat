package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.conditions.StringConditionVisitor;

/**
 * Renames a property
 *
 * @author daniel.mueller
 */
public class RenamePropertyCommand extends AbstractSimpleCommand {

    private static final long serialVersionUID = 1L;
    private String _newPropertyName;

    RenamePropertyCommand() {
    }

    /**
     * @param entityTypeName : the name of the entity to work with.
     * @param property       : the name of the property that should be renamed.
     * @param newProperty    : the new name for the property.
     */
    public RenamePropertyCommand(String entityTypeName, String property, String newProperty) {
        super(entityTypeName, property);
        _newPropertyName = newProperty;
    }

    public String getTargetProperty() {
        return _newPropertyName;
    }

    public void setTargetProperty(String targetProperty) {
        this._newPropertyName = targetProperty;
    }

    @Override
    public String toString() {
        String commandAsString = "rename " + this.getEntityType() + "." + this.getPropertyName() + " to "
                + this.getTargetProperty();

        if (this.getCondition() != null) {
            StringConditionVisitor visitor = new StringConditionVisitor(this._entityType, true);
            this.getCondition().accept(visitor);
            commandAsString += visitor.getConditionAsString();
        }

        return commandAsString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((_newPropertyName == null) ? 0 : _newPropertyName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RenamePropertyCommand other = (RenamePropertyCommand) obj;
        if (_newPropertyName == null) {
            return other._newPropertyName == null;
        } else {
            return _newPropertyName.equals(other._newPropertyName);
        }
    }

}
