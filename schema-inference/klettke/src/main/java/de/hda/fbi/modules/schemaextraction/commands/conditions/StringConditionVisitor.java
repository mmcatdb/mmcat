package de.hda.fbi.modules.schemaextraction.commands.conditions;

import de.hda.fbi.modules.schemaextraction.commands.conditions.Condition.DefaultCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author daniel.mueller
 */
public class StringConditionVisitor implements ConditionVisitor<Void> {

    List<String> conditions = new ArrayList<String>();

    public StringConditionVisitor(String entityName, boolean acceptOnlyValueConditionOnSameEntityType) {
    }

    public String getConditionAsString() {
        String conditionAsString = "";

        for (String conditon : this.conditions) {
            if (conditionAsString == "") {
                conditionAsString += " where " + conditon;
            } else {
                conditionAsString += " and " + conditon;
            }
        }

        return conditionAsString;
    }

    public Void visit(JoinCondition condition) {

        this.conditions.add(condition.getLeftEntity() + "." + condition.getLeftProperty() + "="
                + condition.getRightEntity() + "." + condition.getRightProperty());

        return null;
    }

    public Void visit(ComplexCondition condition) {
        condition.getLeftCondition().accept(this);
        condition.getRightCondition().accept(this);
        return null;
    }

    public Void visit(DefaultCondition condition) {
        // TODO Auto-generated method stub
        return null;
    }

}
