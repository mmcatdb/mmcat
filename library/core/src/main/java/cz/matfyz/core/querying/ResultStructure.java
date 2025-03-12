package cz.matfyz.core.querying;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.GraphUtils;
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
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.Nullable;

// TODO add to json conversion for FE.
public class ResultStructure implements Tree<ResultStructure>, Printable, Serializable {

    public final String name;
    /**
     * If true, there is an array between this node and its parent (if there is no parent, then there is just the array).
     * This doesn't depend only on the signature. E.g., a filter or redundancy might cause an array to become a 1:1 relationship.
     * A path is an array only from parent to child, not the other way around - there is always at most one parent for a child.
     */
    public final boolean isArray;
    /** Each result structure node corresponds to a variable. */
    public final Variable variable;
    private final Map<Signature, ResultStructure> children = new TreeMap<>();

    /** If null, this is the root of the tree. */
    @JsonIgnore
    @Nullable private ResultStructure parent;
    /** If null, this is the root of the tree. */
    @Nullable private Signature signatureFromParent;

    public ResultStructure(String name, boolean isArray, Variable variable) {
        this.name = name;
        this.isArray = isArray;
        this.variable = variable;
    }

    public @Nullable Signature getSignatureFromParent() {
        return signatureFromParent;
    }

    /**
     * Adds the child and returns it back.
     */
    public ResultStructure addChild(ResultStructure child, Signature signature) {
        this.children.put(signature, child);
        child.parent = this;
        child.signatureFromParent = signature;

        return child;
    }

    /**
     * Removes the child and returns it back. Throws an exception if the child is not found.
     */
    public ResultStructure removeChild(Signature signature) {
        final var child = children.get(signature);
        if (child == null)
            throw new RuntimeException("Child not found for signature " + signature);

        children.remove(signature);
        child.parent = null;
        child.signatureFromParent = null;

        return child;
    }

    public @Nullable ResultStructure getChild(Signature signature) {
        return children.get(signature);
    }

    // TODO Not very efficient. We could probably do better (if we had some guarantees about the structure and the variable).
    public @Nullable ResultStructure tryFindDescendantByVariable(Variable variable) {
        return GraphUtils.findDFS(this, node -> node.variable.equals(variable));
    }

    public ResultStructure findDescendantByVariable(Variable variable) {
        final var output = tryFindDescendantByVariable(variable);
        if (output == null)
            throw new RuntimeException("ResultStructure not found for variable " + variable);

        return output;
    }

    /** Traverses given signature as far as possible. Returns the last result structure. */
    public ResultStructure traverseSignature(Signature path) {
        ResultStructure current = this;
        Signature currentSignature = Signature.createEmpty();

        for (final var base : path.toBases()) {
            currentSignature = currentSignature.concatenate(base);
            final var found = current.getChild(currentSignature);
            if (found != null) {
                current = found;
                currentSignature = Signature.createEmpty();
            }
        }

        return current;
    }

    /**
     * Returns the path from the root (excluded) of the structure tree all the way to this instance (also excluded).
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

    /** Returns the signature from the root to this structure. */
    public Signature getSignatureFromRoot() {
        if (this.signatureFromParent == null)
            return Signature.createEmpty();

        return Signature.concatenate(
            getPathFromRoot().stream().map(ResultStructure::getSignatureFromParent),
            // The path from root excludes this node so we have to add its signature manually.
            Stream.of(this.signatureFromParent)
        );
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

    public Collection<Signature> childSignatures() {
        return this.children.keySet();
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
     * Creates a deep copy of the structure. The result will be a root of a new tree, i.e., the parent and the signatureFromParent will be null.
     * The ability to change the array-ness is important because the new structure might be used in a different way than the original one. E.g., a root of one structure is inserted as a subtree to another structure.
     * @param isArray Whether the new structure should be an array.
     */
    public ResultStructure copy(boolean isArray) {
        final var clone = new ResultStructure(name, isArray, variable);

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
            printer.append(child.signatureFromParent).append(": ");
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
