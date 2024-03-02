package cz.matfyz.core.querying;

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
    private final Map<String, QueryStructure> children = new TreeMap<>();

    /** If null, this is the root of the tree. */
    @Nullable
    private QueryStructure parent;

    public QueryStructure(String name, boolean isArray) {
        this.name = name;
        this.isArray = isArray;
    }

    /**
     * Adds the child and returns it back.
     */
    public QueryStructure addChild(QueryStructure child) {
        this.children.put(child.name, child);
        child.parent = this;
        return child;
    }

    /**
     * Returns the path from the root (not included) of the structure tree all the way to this instance (also not included).
     */
    public List<QueryStructure> getParentPath() {
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

    @Override public Collection<QueryStructure> children() {
        return this.children.values();
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

    @Override public int compareTo(QueryStructure other) {
        return name.compareTo(other.name);
    }
}
