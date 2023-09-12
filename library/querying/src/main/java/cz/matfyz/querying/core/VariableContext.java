package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.VariableIdentifier;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.parsing.Variable;

import java.util.Map;
import java.util.TreeMap;

public class VariableContext {
    
    private final Map<VariableIdentifier, SchemaObject> objects = new TreeMap<>();

    public VariableContext defineVariable(Variable variable, SchemaObject object) {
        objects.put(variable.id, object);

        return this;
    }

    public SchemaObject getObject(Variable variable) {
        return objects.get(variable.id);
    }

}
