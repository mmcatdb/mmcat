package cz.matfyz.core.querying;

import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.printable.*;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

// TODO add to json conversion for FE. Also, probably move to a separate file.
public class QueryStructure implements Tree<QueryStructure>, Printable {

    public final String name;
    // TODO find out if the object is needed
    public final boolean isArray;
    public final Map<String, QueryStructure> children = new TreeMap<>();

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
