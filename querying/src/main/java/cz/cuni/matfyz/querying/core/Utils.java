package cz.cuni.matfyz.querying.core;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.KindInstance;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.querying.exception.GeneralException;
import cz.cuni.matfyz.querying.parsing.Query;
import cz.cuni.matfyz.querying.parsing.Variable;
import cz.cuni.matfyz.querying.parsing.WhereTriple;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public abstract class Utils {

    private Utils() {}

    /**
     * Get the set of variables from the query, along with the corresponding schema object for each variable.
     */
    public static Map<String, SchemaObject> getVariableTypesFromQuery(Query query, SchemaCategory schemaCategory) {
        return getVariableTypes(query.where.triples, schemaCategory);
    }

    /**
     * Get the set of variables from the query part, along with the corresponding schema object for each variable.
     */
    public static Map<String, SchemaObject> getVariableTypesFromPart(QueryPart part, SchemaCategory schemaCategory) {
        var triples = part.triplesMapping.stream().map(t -> t.triple).toList();
        return getVariableTypes(triples, schemaCategory);
    }

    /**
     * Get the set of variables from the provided set of triples, along with the corresponding schema object for each variable.
     */
    public static Map<String, SchemaObject> getVariableTypes(List<WhereTriple> triples, SchemaCategory schemaCategory) {
        var variableTypes = new TreeMap<String, SchemaObject>();

        for (var triple : triples) {
            var morphism = schemaCategory.getMorphism(triple.signature);
            var subjectType = morphism.dom();
            var objectType = morphism.cod();

            if (!variableTypes.containsKey(triple.subject.name))
                variableTypes.put(triple.subject.name, subjectType);
            else if (!variableTypes.get(triple.subject.name).equals(subjectType))
                throw GeneralException.message("Variable " + triple.subject.name + " has conflicting types");

            if (triple.object instanceof Variable variable) {
                if (!variableTypes.containsKey(variable.name))
                    variableTypes.put(variable.name, objectType);
                else if (!variableTypes.get(variable.name).equals(objectType))
                    throw GeneralException.message("Variable " + variable.name + " has conflicting types");
            }
        }

        return variableTypes;
    }

    /**
     * Given a query part, return the set of kinds occurring in this query part.
     * Note that there can be multiple kinds for multiple variables which refer to the same underlying database kind.
     */
    public static List<KindInstance> getKindsFromPart(QueryPart part) {
        // TODO this sus
        return part.triplesMapping.stream().map(t -> t.kind).toList();
    }

    public static boolean isObjectTerminal(SchemaObject object) {
        return !object.ids().isSignatures();
    }

    public static List<AccessPath> getPropertyPath(Mapping mapping, Signature signature) {
        var a = signature.toString();
        final var path = mapping.getPropertyPath(signature);
        if (path == null)
            return null;

        // TODO - Dynamic names are not yet supported. After this is solved, the original method on Mapping should be used instead.
        // TODO - There might be a valid path in some other branch of the access path tree.
        if (path.get(path.size() - 1).name() instanceof DynamicName)
            return null;

        return path;
    }

}
