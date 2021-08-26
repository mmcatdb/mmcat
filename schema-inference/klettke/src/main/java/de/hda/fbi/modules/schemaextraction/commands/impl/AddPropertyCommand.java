package de.hda.fbi.modules.schemaextraction.commands.impl;

import de.hda.fbi.modules.schemaextraction.commands.conditions.StringConditionVisitor;
import de.hda.fbi.modules.schemaextraction.common.DataType;

/**
 * Adds an property to the provided schema and returns it.
 *
 * @author daniel.mueller
 */
public class AddPropertyCommand extends AbstractSimpleCommand {

    private static final long serialVersionUID = 1L;
    private DataType _dataType;
    private Object _defaultValue;
    private boolean _isVersion = false;
    private boolean _isId = false;
    private Integer _min = null;
    private Integer _max = null;
    private boolean _required = false;
    private boolean _notNull;

    AddPropertyCommand() {
    }

    /**
     * @param entityName : the name of the entity to work with.
     * @param property   : the new property to add.
     */
    public AddPropertyCommand(String entityName, String property) {
        super(entityName, property);
    }

    public DataType getDataTypeValue() {
        return _dataType;
    }

    public Class<?> getDataType() {
        return _dataType.getClassFromEnum();
    }

    public void setDataType(DataType dataType) {
        _dataType = dataType;
    }

    public Object getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        _defaultValue = defaultValue;
    }

    public <T> T getDefaultValue(Class<T> clazz) {
        return clazz.cast(_defaultValue);
    }

    public boolean isDarwin() {
        return _isVersion;
    }

    void setDarwin(boolean isDarwin) {
        _isVersion = isDarwin;
    }

    public boolean isId() {
        return _isId;
    }

    void setId(boolean isId) {
        _isId = isId;
    }

    public Integer getMin() {
        return _min;
    }

    void setMin(Integer min) {
        _min = min;
    }

    public Integer getMax() {
        return _max;
    }

    void setMax(Integer max) {
        _max = max;
    }

    public boolean isRequired() {
        return _required;
    }

    void setRequired(boolean required) {
        _required = required;
    }

    public boolean isNullDisallowed() {
        return _notNull;
    }

    void setNullDisallowed(boolean notNull) {
        _notNull = notNull;
    }

    @Override
    public String toString() {
        String commandAsString = "add " + this.getDataTypeAsString() + " " + this.getEntityType() + "."
                + getPropertyName();

        if (this.getDefaultValue() != null) {
            commandAsString += " = " + this.getDefaultValue();
        }

        boolean attributeInUse = false;

        if (this.isRequired()) {
            commandAsString += " as required";
            attributeInUse = true;
        }

        if (this.getMin() != null) {
            if (attributeInUse) {
                commandAsString += ", min=" + this.getMin();
            } else {
                commandAsString += " as min=" + this.getMin();
            }
        }

        if (this.getMax() != null) {
            if (attributeInUse) {
                commandAsString += ", max=" + this.getMax();
            } else {
                commandAsString += " as max=" + this.getMax();
            }
        }

        if (this.getCondition() != null) {
            StringConditionVisitor visitor = new StringConditionVisitor(this._entityType, true);
            this.getCondition().accept(visitor);
            commandAsString += visitor.getConditionAsString();
        }

        return commandAsString;
    }

    private String getDataTypeAsString() {

        // This only occurs in test cases
        if (this._dataType == null) {
            return "";
        }

        switch (this._dataType) {
            case STRING:
                return "string";
            case INTEGER:
                return "integer";
            case BOOLEAN:
                return "boolean";
            case DATE:
                return "date";
            case DOUBLE:
                return "double";
            case FLOAT:
                return "float";
            case LONG:
                return "long";
        }

        return "#####___WRONG DATA TYPE #####";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_dataType == null) ? 0 : _dataType.hashCode());
        result = prime * result + ((_defaultValue == null) ? 0 : _defaultValue.hashCode());
        result = prime * result + (_isId ? 1231 : 1237);
        result = prime * result + (_isVersion ? 1231 : 1237);
        result = prime * result + ((_max == null) ? 0 : _max.hashCode());
        result = prime * result + ((_min == null) ? 0 : _min.hashCode());
        result = prime * result + (_notNull ? 1231 : 1237);
        result = prime * result + (_required ? 1231 : 1237);
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
        AddPropertyCommand other = (AddPropertyCommand) obj;
        if (_dataType != other._dataType) {
            return false;
        }
        if (_defaultValue == null) {
            if (other._defaultValue != null) {
                return false;
            }
        } else if (!_defaultValue.equals(other._defaultValue)) {
            return false;
        }
        if (_isId != other._isId) {
            return false;
        }
        if (_isVersion != other._isVersion) {
            return false;
        }
        if (_max == null) {
            if (other._max != null) {
                return false;
            }
        } else if (!_max.equals(other._max)) {
            return false;
        }
        if (_min == null) {
            if (other._min != null) {
                return false;
            }
        } else if (!_min.equals(other._min)) {
            return false;
        }
        if (_notNull != other._notNull) {
            return false;
        }
        return _required == other._required;
    }

}
