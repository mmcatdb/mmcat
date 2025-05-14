package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.StringName;

public class Neo4jPathWrapper implements AbstractPathWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return false; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return false; }
    @Override public boolean isGroupingAllowed() { return false; }
    @Override public boolean isReferenceAllowed() { return false; }
    @Override public boolean isComplexPropertyAllowed() { return true; } // Just for the _from and _to nodes, false otherwise.
    @Override public boolean isSchemaless() { return true; }
    // CHECKSTYLE:ON

    @Override public boolean isPathValid(ComplexProperty accessPath) {
        // Check for the special nodes that represent a relationship.
        boolean hasFromNode = false;
        boolean hasToNode = false;

        for (final var subpath : accessPath.subpaths()) {
            if (!(subpath.name() instanceof StringName name))
                continue;

            final var isFrom = name.value.startsWith(Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX);
            final var isTo = name.value.startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);
            if (!isFrom && !isTo)
                continue;

            // Now we know it's the special node property.

            if (!(subpath instanceof ComplexProperty complex))
                // The special node property must be a complex property.
                return false;

            if (complex.subpaths().size() != 1)
                // The special node property must have exactly one subpath, the identifier.
                return false;

            // TODO Maybe we can check that the identifier is truly an id of some kind? But we would need the schema for that.

            if (isFrom) {
                if (hasFromNode)
                    // There can be only one _from node.
                    return false;
                hasFromNode = true;
            } else {
                if (hasToNode)
                    // There can be only one _to node.
                    return false;
                hasToNode = true;
            }
        }

        if (!hasFromNode && !hasToNode)
            // It's a normal node, not a relationship.
            return true;

        if (!hasFromNode || !hasToNode)
            // It's a relationship, but it's missing one of the special nodes.
            return false;

        // It's a relationship with both special nodes. Everything is fine.
        return true;
    }

}
