package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory.InstancePath;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.record.IComplexRecord;
import cz.matfyz.core.utils.printable.*;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class StackTriple implements Printable {
    
    public final InstancePath parentToChild;
    public final DomainRow parentRow;
    public final AccessPath childAccessPath;
    public final IComplexRecord parentRecord;
    
    public StackTriple(DomainRow parentRow, InstancePath parentToChild, AccessPath childAccessPath, IComplexRecord parentRecord) {
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
