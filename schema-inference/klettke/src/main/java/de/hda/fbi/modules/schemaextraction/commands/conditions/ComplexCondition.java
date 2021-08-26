package de.hda.fbi.modules.schemaextraction.commands.conditions;

public class ComplexCondition implements Condition {

    private static final long serialVersionUID = 1L;
    private Condition _left;
    private Condition _right;

    public ComplexCondition() {
    }

    public ComplexCondition(Condition c) {
        this(c, Condition.TRUE);
    }

    public ComplexCondition(Condition left, Condition right) {
        if (left == null || right == null) {
            throw new NullPointerException("Can not set null condition.");
        }

        _left = left;
        _right = right;
    }

	@Override
    public <T> T accept(ConditionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Condition getLeftCondition() {
        return _left;
    }

    public Condition getRightCondition() {
        return _right;
    }

    @Override
    public String toString() {
        return "(" + getLeftCondition().toString() + " AND " + getRightCondition().toString() + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_left == null) ? 0 : _left.hashCode());
        result = prime * result + ((_right == null) ? 0 : _right.hashCode());
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
        ComplexCondition other = (ComplexCondition) obj;
        if (_left == null) {
            if (other._left != null) {
                return false;
            }
        } else if (!_left.equals(other._left)) {
            return false;
        }
        if (_right == null) {
            return other._right == null;
        } else {
            return _right.equals(other._right);
        }
    }

    public void append(Condition condition) {
        if (_left == null) {
            _left = condition;
            return;
        }

        if (_right == null) {
            _right = condition;
            return;
        }

        if (!(_left instanceof ComplexCondition)) {
            _left = new ComplexCondition(_left, condition);
            return;
        }

        if (!(_right instanceof ComplexCondition)) {
            _right = new ComplexCondition(_right, condition);
            return;
        }

        if (_left instanceof ComplexCondition) {
            ComplexCondition left = (ComplexCondition) _left;
            left.append(condition);
        }

    }
}
