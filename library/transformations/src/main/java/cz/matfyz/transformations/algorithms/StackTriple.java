package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.printable.*;

import org.checkerframework.checker.nullness.qual.Nullable;

public class StackTriple implements Printable {

    public final DomainRow parentRow;
    public final ComplexRecord parentRecord;
    public final SchemaPath parentToChild;

    /**
     * If null, there is no access path associated with the row.
     * E.g., the property has signature <code>1.2</code>, but the object corresponds to the cod object of <code>1</code>.
     */
    public final @Nullable ComplexProperty childAccessPath;

    public StackTriple(DomainRow parentRow, ComplexRecord parentRecord, SchemaPath parentToChild, @Nullable ComplexProperty childAccessPath) {
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
