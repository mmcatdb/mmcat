package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.printable.*;

public class StackTriple implements Printable {

    public final DomainRow parentRow;
    public final ComplexRecord parentRecord;
    public final SchemaPath parentToChild;


    // TODO parent to child is the path to the objex.
    // TODO the following comment. If childAccessPath is simple, then it's signature isn't the same as parentToChild.signature(). If complex, then it is.

    /**
     * If null, there is no {@link ComplexProperty} associated with the row.
     * E.g., there is a {@link SimpleProperty} with signature <code>1.2</code>, but the child object is the dom of the last morphism (i.e., <code>1</code>).
     */
    public final AccessPath childAccessPath;

    public StackTriple(DomainRow parentRow, ComplexRecord parentRecord, SchemaPath parentToChild, AccessPath childAccessPath) {
        this.parentRow = parentRow;
        this.parentRecord = parentRecord;
        this.parentToChild = parentToChild;
        this.childAccessPath = childAccessPath;
    }

    @Override public void printTo(Printer printer) {
        printer.append("<").down().nextLine();

        printer.append("parentToChildPath: ").append(parentToChild.signature()).append(",").nextLine();
        printer.append("parentRow: ").append(parentRow).append(",").nextLine();
        printer.append("childAccessPath: ")
            .append("{").down().nextLine()
            .append(childAccessPath == null ? "null" : childAccessPath)
            .up().nextLine().append("}").append(",").nextLine();
        printer.append("record: ").append(parentRecord).append(",");

        printer.up().nextLine().append(">");
    }

    @Override public String toString() {
        return Printer.print(this);
    }
}
