package de.hda.fbi.modules.schemaextraction.commands.conditions;

/**
 * @author daniel.mueller
 */
public class JoinCondition implements Condition {

    private static final long serialVersionUID = -9154980269674522195L;
    private String _leftEntity;
    private String _rightEntity;
    private String _leftProperty;
    private String _rightProperty;

    JoinCondition() {
    }

    public JoinCondition(String leftEntity, String leftProperty, String rightEntity, String rightProperty) {
        _leftEntity = leftEntity;
        _leftProperty = leftProperty;
        _rightProperty = rightProperty;
        _rightEntity = rightEntity;
    }

    public <T> T accept(ConditionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getLeftEntity() {
        return _leftEntity;
    }

    public String getRightEntity() {
        return _rightEntity;
    }

    public String getLeftProperty() {
        return _leftProperty;
    }

    public String getRightProperty() {
        return _rightProperty;
    }

    @Override
    public String toString() {
        return _leftEntity + "." + _leftProperty + " = " + _rightEntity + "." + _rightProperty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_leftEntity == null) ? 0 : _leftEntity.hashCode());
        result = prime * result + ((_leftProperty == null) ? 0 : _leftProperty.hashCode());
        result = prime * result + ((_rightEntity == null) ? 0 : _rightEntity.hashCode());
        result = prime * result + ((_rightProperty == null) ? 0 : _rightProperty.hashCode());
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
        JoinCondition other = (JoinCondition) obj;
        if (_leftEntity == null) {
            if (other._leftEntity != null) {
                return false;
            }
        } else if (!_leftEntity.equals(other._leftEntity)) {
            return false;
        }
        if (_leftProperty == null) {
            if (other._leftProperty != null) {
                return false;
            }
        } else if (!_leftProperty.equals(other._leftProperty)) {
            return false;
        }
        if (_rightEntity == null) {
            if (other._rightEntity != null) {
                return false;
            }
        } else if (!_rightEntity.equals(other._rightEntity)) {
            return false;
        }
        if (_rightProperty == null) {
            return other._rightProperty == null;
        } else {
            return _rightProperty.equals(other._rightProperty);
        }
    }

}
