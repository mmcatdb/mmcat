package cz.matfyz.core.querying;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.printable.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.Nullable;

// TODO add to json conversion for FE.
public class ResultStructure implements Tree<ResultStructure>, Printable, Serializable {

    public final String name;
    public final boolean isArray;
    /** Each result structure node corresponds to a variable. */
    public final Variable variable;
    private final Map<String, ResultStructure> children = new TreeMap<>();
    private final Map<Signature, ResultStructure> childrenBySignature = new TreeMap<>();

    /** If null, this is the root of the tree. */
    @JsonIgnore
    @Nullable private ResultStructure parent;
    /** If null, this is the root of the tree. */
    @Nullable public Signature signatureFromParent;

    public ResultStructure(String name, boolean isArray, Variable variable) {
        this.name = name;
        this.isArray = isArray;
        this.variable = variable;
    }

    /**
     * Adds the child and returns it back.
     */
    public ResultStructure addChild(ResultStructure child, Signature signature) {
        this.children.put(child.name, child);
        this.childrenBySignature.put(signature, child);
        child.parent = this;
        child.signatureFromParent = signature;

        return child;
    }

    public ResultStructure getChild(String name) {
        return children.get(name);
    }

    // TODO Not very efficient and also nullable. We could probably do better (if we had some guarantees about the structure and the signature).
    public @Nullable ResultStructure tryFindDescendantBySignature(Signature path) {
        final var bases = path.toBases();

        Signature current = Signature.createEmpty();
        for (int i = 0; i < bases.size(); i++) {
            current = current.concatenate(bases.get(i));
            final var child = childrenBySignature.get(current);
            if (child == null)
                continue;

            if (i == bases.size() - 1)
                return child;

            return child.tryFindDescendantBySignature(path.cutPrefix(current));
        }

        return null;
    }

    // TODO Not very efficient and also nullable. We could probably do better (if we had some guarantees about the structure and the variable).
    public @Nullable ResultStructure tryFindDescendantByVariable(Variable variable) {
        if (this.variable.equals(variable))
            return this;

        for (final var child : children.values()) {
            final var output = child.tryFindDescendantByVariable(variable);
            if (output != null)
                return output;
        }

        return null;
    }

    public ResultStructure removeChild(String name) {
        final var output = children.remove(name);
        childrenBySignature.remove(output.signatureFromParent);
        return output;
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

    /** All computations whose reference node is this object. */
    private final Set<Computation> computations = new TreeSet<>();

    public boolean hasComputation(Computation computation) {
        return computations.contains(computation);
    }

    public boolean addComputation(Computation computation) {
        return computations.add(computation);
    }

    /**
     * The ability to change the array-ness is important because the new structure might be used in a different way than the original one. E.g., a root of one structure is inserted as a subtree to another structure.
     * @param isArray Whether the new structure should be an array.
     */
    public ResultStructure copy(boolean isArray) {
        final var clone = new ResultStructure(name, isArray, variable);
        clone.parent = parent;
        clone.signatureFromParent = signatureFromParent;

        children.values().forEach(child -> clone.addChild(child.copy(), child.signatureFromParent));

        computations.forEach(clone.computations::add);

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
