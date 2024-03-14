package cz.matfyz.core.querying;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.printable.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

// TODO add to json conversion for FE.
public class QueryStructure implements Tree<QueryStructure>, Printable {

    public final String name;
    public final boolean isArray;
    /** Each query structure node corresponds to a schema object. Multiple structures might correspond to the same object. */
    public final SchemaObject schemaObject;
    private final Map<String, QueryStructure> children = new TreeMap<>();

    /** If null, this is the root of the tree. */
    @Nullable
    private QueryStructure parent;
    /** If null, this is the root of the tree. */
    @Nullable
    public Signature signatureFromParent;

    public QueryStructure(String name, boolean isArray, SchemaObject schemaObject) {
        this.name = name;
        this.isArray = isArray;
        this.schemaObject = schemaObject;
    }

    /**
     * Adds the child and returns it back.
     */
    public QueryStructure addChild(QueryStructure child, Signature signature) {
        this.children.put(child.name, child);
        child.parent = this;
        child.signatureFromParent = signature;
        
        return child;
    }

    public QueryStructure getChild(String name) {
        return children.get(name);
    }

    public QueryStructure removeChild(String name) {
        return children.remove(name);
    }

    /**
     * Returns the path from the root (not included) of the structure tree all the way to this instance (also not included).
     */
    public List<QueryStructure> getPathFromRoot() {
        final List<QueryStructure> path = new ArrayList<>();
        QueryStructure current = this;

        while (current.parent() != null) {
            path.add(current.parent());
            current = current.parent();
        }
        if (!path.isEmpty())
            path.removeLast();

        return path.reversed();
    }

    @Nullable
    public QueryStructure parent() {
        return parent;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override public Collection<QueryStructure> children() {
        return this.children.values();
    }

    /**
     * The ability to change the array-ness is important because the new structure might be used in a different way than the original one. E.g., a root of one structure is inserted as a subtree to another structure.
     * @param isArray Whether the new structure should be an array.
     */
    public QueryStructure copy(boolean isArray) {
        final var clone = new QueryStructure(name, isArray, schemaObject);
        clone.parent = parent;
        clone.signatureFromParent = signatureFromParent;

        children.values().forEach(child -> clone.addChild(child.copy(), child.signatureFromParent));

        return clone;
    }

    /** A convenience method to copy without changing the isArray. */
    public QueryStructure copy() {
        return copy(isArray);
    }

    @Override public void printTo(Printer printer) {
        printer.append(name);
        if (isArray)
            printer.append("[]");

        if (!children.isEmpty())
            printer.append(":");

        printer
            .down()
            .nextLine();

        children.values().forEach(child -> {
            child.printTo(printer);
            printer.nextLine();
        });
        printer.remove().up();
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    @Override public boolean equals(Object object) {
        return object instanceof QueryStructure structure && compareTo(structure) == 0;
    }

    @Override public int compareTo(QueryStructure other) {
        return name.compareTo(other.name);
    }
}
