package cz.matfyz.core.querying;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.printable.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.Nullable;

// TODO add to json conversion for FE.
public class ResultStructure implements Tree<ResultStructure>, Printable, Serializable {

    public final String name;
    public final boolean isArray;
    /** Each result structure node corresponds to a schema object. Multiple structures might correspond to the same object. */
    @JsonIgnore
    public final SchemaObject schemaObject;
    private final Map<String, ResultStructure> children = new TreeMap<>();

    /** If null, this is the root of the tree. */
    @JsonIgnore
    @Nullable private ResultStructure parent;
    /** If null, this is the root of the tree. */
    @Nullable public Signature signatureFromParent;

    public ResultStructure(String name, boolean isArray, SchemaObject schemaObject) {
        this.name = name;
        this.isArray = isArray;
        this.schemaObject = schemaObject;
    }

    /**
     * Adds the child and returns it back.
     */
    public ResultStructure addChild(ResultStructure child, Signature signature) {
        this.children.put(child.name, child);
        child.parent = this;
        child.signatureFromParent = signature;

        return child;
    }

    public ResultStructure getChild(String name) {
        return children.get(name);
    }

    public ResultStructure removeChild(String name) {
        return children.remove(name);
    }

    /**
     * Returns the path from the root (not included) of the structure tree all the way to this instance (also not included).
     */
    public List<ResultStructure> getPathFromRoot() {
        final List<ResultStructure> path = new ArrayList<>();
        ResultStructure current = this;

        while (current.parent() != null) {
            path.add(current.parent());
            current = current.parent();
        }
        if (!path.isEmpty())
            path.removeLast();

        return path.reversed();
    }

    public @Nullable ResultStructure parent() {
        return parent;
    }

    @JsonIgnore
    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override public Collection<ResultStructure> children() {
        return this.children.values();
    }

    /**
     * The ability to change the array-ness is important because the new structure might be used in a different way than the original one. E.g., a root of one structure is inserted as a subtree to another structure.
     * @param isArray Whether the new structure should be an array.
     */
    public ResultStructure copy(boolean isArray) {
        final var clone = new ResultStructure(name, isArray, schemaObject);
        clone.parent = parent;
        clone.signatureFromParent = signatureFromParent;

        children.values().forEach(child -> clone.addChild(child.copy(), child.signatureFromParent));

        return clone;
    }

    /** A convenience method to copy without changing the isArray. */
    public ResultStructure copy() {
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
        return object instanceof ResultStructure structure && compareTo(structure) == 0;
    }

    @Override public int compareTo(ResultStructure other) {
        return name.compareTo(other.name);
    }
}
