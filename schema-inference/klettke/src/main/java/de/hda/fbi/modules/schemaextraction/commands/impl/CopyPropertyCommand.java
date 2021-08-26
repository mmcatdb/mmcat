package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.conditions.StringConditionVisitor;

/**
 * Copies a property of one schema to another schema.
 *
 * @author daniel.mueller
 */

public class CopyPropertyCommand extends AbstractComplexCommand {

    private static final long serialVersionUID = 1L;

    CopyPropertyCommand() {

    }

    /**
     * @param sourceEntityType : the name of entity to work with.
     * @param sourceProperty   : the name of the property to work with.
     * @param targetEntityType : the schema that gets the property.
     */
    public CopyPropertyCommand(String sourceEntityType, String sourceProperty, String targetEntityType) {
        super(sourceEntityType, sourceProperty);
        setTargetEntityType(targetEntityType);
        setTargetProperty(sourceProperty);
    }

    public CopyPropertyCommand(String sourceEntityType, String sourceProperty, String targetEntityType,
                               String targetPropertyType) {
        super(sourceEntityType, sourceProperty);
        setTargetEntityType(targetEntityType);
        setTargetProperty(targetPropertyType);
    }

    @Override
    public String toString() {
        String commandAsString = "copy " + this.getEntityType() + "." + this.getPropertyName() + " to "
                + this.getTargetEntityType() + "." + this.getTargetProperty();

        if (this.getCondition() != null) {
            StringConditionVisitor visitor = new StringConditionVisitor(this._sourceEntityType, false);
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
