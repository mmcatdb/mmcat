package de.hda.fbi.modules.schemaextraction.commands.conditions;

import de.hda.fbi.modules.schemaextraction.commands.conditions.Condition.DefaultCondition;

public interface ConditionVisitor<T> {

    T visit(JoinCondition condition);

    T visit(ComplexCondition condition);

    T visit(DefaultCondition condition);
}
