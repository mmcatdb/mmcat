package de.hda.fbi.modules.schemaextraction.commands.conditions;

import java.io.Serializable;

/**
 * @author daniel.mueller
 */
public interface Condition extends Serializable {

    /**
     * A condition always evaluating to {@code true}.
     */
    Condition TRUE = new DefaultCondition(true);

    /**
     * A condition always evaluating to {@code false}.
     */
    Condition FALSE = new DefaultCondition(false);

    <T> T accept(ConditionVisitor<T> visitor);

    final class DefaultCondition implements Condition {

        private static final long serialVersionUID = 1L;
        private boolean _eval;

        private DefaultCondition() {
        }

        private DefaultCondition(boolean eval) {
            _eval = eval;
        }

        @Override
        public String toString() {
            return Boolean.toString(_eval);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (_eval ? 1231 : 1237);
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
            DefaultCondition other = (DefaultCondition) obj;
            return _eval == other._eval;
        }

        public <T> T accept(ConditionVisitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}
