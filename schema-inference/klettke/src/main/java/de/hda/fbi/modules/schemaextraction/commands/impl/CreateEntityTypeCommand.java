package de.hda.fbi.modules.schemaextraction.commands.impl;

/**
 * @author daniel.mueller
 */
public class CreateEntityTypeCommand extends AbstractSimpleCommand {

    private static final long serialVersionUID = 1L;

    CreateEntityTypeCommand() {
    }

    public CreateEntityTypeCommand(String entityTypeName) {
        super(entityTypeName);
    }

    @Override
    public String toString() {
        return "create " + this.getEntityType();
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
