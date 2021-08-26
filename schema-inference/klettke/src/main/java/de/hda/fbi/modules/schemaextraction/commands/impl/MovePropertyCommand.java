package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.conditions.StringConditionVisitor;

/**
 * Moves a property from one schema to another
 *
 * @author daniel.mueller
 */
public class MovePropertyCommand extends AbstractComplexCommand {

    private static final long serialVersionUID = 1L;
    private CopyPropertyCommand _copyPropertyCommand;
    private DeletePropertyCommand _deletePropertyCommand;
    private RenamePropertyCommand _renamePropertyCommand;

    MovePropertyCommand() {
    }

    public MovePropertyCommand(String sourceEntityType, String sourceProperty, String targetEntityType) {
        super(sourceEntityType, sourceProperty);
        setTargetEntityType(targetEntityType);
        _copyPropertyCommand = new CopyPropertyCommand(sourceEntityType, sourceProperty, targetEntityType);
        _deletePropertyCommand = new DeletePropertyCommand(sourceEntityType, sourceProperty);
    }

    public MovePropertyCommand(String sourceEntityType, String sourceProperty, String targetEntityType,
                               String targetProperty) {
        super(sourceEntityType, sourceProperty);
        setTargetEntityType(targetEntityType);
        setTargetProperty(targetProperty);
        _copyPropertyCommand = new CopyPropertyCommand(sourceEntityType, sourceProperty, targetEntityType,
                sourceProperty);

        if (!sourceProperty.equals(targetProperty)) {
            _renamePropertyCommand = new RenamePropertyCommand(targetEntityType, sourceProperty, targetProperty);
        }
        _deletePropertyCommand = new DeletePropertyCommand(sourceEntityType, sourceProperty);
    }

    @Override
    public String getTargetEntityType() {
        return _copyPropertyCommand.getTargetEntityType();
    }

    @Override
    public String getTargetProperty() {

        if (_renamePropertyCommand != null) {
            return _renamePropertyCommand.getTargetProperty();
        }
        return _copyPropertyCommand.getTargetProperty();
    }

    @Override
    public String toString() {
        String commandAsString = "move " + this.getEntityType() + "." + this.getPropertyName() + " to "
                + this.getTargetEntityType() + "." + this.getTargetProperty();

        if (this.getCondition() != null) {
            StringConditionVisitor visitor = new StringConditionVisitor(this._sourceEntityType, false);
            this.getCondition().accept(visitor);
            commandAsString += visitor.getConditionAsString();
        }

        return commandAsString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((_copyPropertyCommand == null) ? 0 : _copyPropertyCommand.hashCode());
        result = prime * result + ((_deletePropertyCommand == null) ? 0 : _deletePropertyCommand.hashCode());
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
        MovePropertyCommand other = (MovePropertyCommand) obj;
        if (_copyPropertyCommand == null) {
            if (other._copyPropertyCommand != null) {
                return false;
            }
        } else if (!_copyPropertyCommand.equals(other._copyPropertyCommand)) {
            return false;
        }
        if (_deletePropertyCommand == null) {
            return other._deletePropertyCommand == null;
        } else {
            return _deletePropertyCommand.equals(other._deletePropertyCommand);
        }
    }

}
