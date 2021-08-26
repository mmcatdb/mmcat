package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.conditions.StringConditionVisitor;

/**
 * Deletes a property from a schema
 *
 * @author daniel.mueller
 */
public class DeletePropertyCommand extends AbstractSimpleCommand {

    private static final long serialVersionUID = 1L;

    DeletePropertyCommand() {
    }

    /**
     * @param entityTypeName : The name of the entity to work with.
     * @param propertyName   : The property that should be deleted.
     */
    public DeletePropertyCommand(String entityTypeName, String propertyName) {
        super(entityTypeName, propertyName);
    }

    @Override
    public String toString() {
        String commandAsString = "delete  " + this.getEntityType() + "." + this.getPropertyName();

        if (this.getCondition() != null) {
            StringConditionVisitor visitor = new StringConditionVisitor(this._entityType, true);
            this.getCondition().accept(visitor);
            commandAsString += visitor.getConditionAsString();
        }

        return commandAsString;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
