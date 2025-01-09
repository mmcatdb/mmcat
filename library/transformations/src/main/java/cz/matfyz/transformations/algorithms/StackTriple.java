package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.record.ComplexRecord;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.printable.*;

public class StackTriple implements Printable {

    public final SchemaPath parentToChild;
    public final DomainRow parentRow;
    public final AccessPath childAccessPath;
    public final ComplexRecord parentRecord;

    public StackTriple(DomainRow parentRow, SchemaPath parentToChild, AccessPath childAccessPath, ComplexRecord parentRecord) {
        this.parentRow = parentRow;
        this.parentToChild = parentToChild;
        this.childAccessPath = childAccessPath;
        this.parentRecord = parentRecord;
    }

    @Override public void printTo(Printer printer) {
        printer.append("<").down().nextLine();

        printer.append("parentToChildPath: ").append(parentToChild.signature()).append(",").nextLine();
        printer.append("parentRow: ").append(parentRow).append(",").nextLine();
        printer.append("childAccessPath: ")
            .append("{").down().nextLine()
            .append(childAccessPath)
            .up().nextLine().append("}").append(",").nextLine();
        printer.append("record: ").append(parentRecord).append(",");

        printer.up().nextLine().append(">");
    }

    @Override public String toString() {
        return Printer.print(this);
    }
}
