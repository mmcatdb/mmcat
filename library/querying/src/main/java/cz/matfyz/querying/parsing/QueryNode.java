package cz.matfyz.querying.parsing;

import cz.matfyz.querying.exception.ParsingException;

import java.io.Serializable;

public abstract class QueryNode implements Serializable {

    Query asQuery() {
        throw ParsingException.wrongNode(Query.class, this);
    }

    SelectClause asSelectClause() {
        throw ParsingException.wrongNode(SelectClause.class, this);
    }

    GroupGraphPattern asGroupGraphPattern() {
        throw ParsingException.wrongNode(GroupGraphPattern.class, this);
    }

    WhereClause asWhereClause() {
        throw ParsingException.wrongNode(WhereClause.class, this);
    }

    Filter asFilter() {
        throw ParsingException.wrongNode(Filter.class, this);
    }

    Variable asVariable() {
        throw ParsingException.wrongNode(Variable.class, this);
    }

    CommonTriplesList asCommonTriplesList() {
        throw ParsingException.wrongNode(CommonTriplesList.class, this);
    }

    SelectTriplesList asSelectTriplesList() {
        throw ParsingException.wrongNode(SelectTriplesList.class, this);
    }

    WhereTriplesList asWhereTriplesList() {
        throw ParsingException.wrongNode(WhereTriplesList.class, this);
    }

    Values asValues() {
        throw ParsingException.wrongNode(Values.class, this);
    }

    MorphismsList asMorphisms() {
        throw ParsingException.wrongNode(MorphismsList.class, this);
    }

    ObjectsList asObjectsList() {
        throw ParsingException.wrongNode(ObjectsList.class, this);
    }

    StringValue asStringValue() {
        throw ParsingException.wrongNode(StringValue.class, this);
    }

    ValueNode asValueNode() {
        throw ParsingException.wrongNode(ValueNode.class, this);
    }

}
