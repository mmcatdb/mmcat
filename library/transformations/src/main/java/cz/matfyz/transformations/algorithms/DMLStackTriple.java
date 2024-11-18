package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.utils.printable.*;

public class DMLStackTriple implements Printable {

    public final DomainRow row;
    public final String name;
    public final ComplexProperty complexProperty;

    public DMLStackTriple(DomainRow row, String name, ComplexProperty t) {
        this.row = row;
        this.name = name;
        this.complexProperty = t;
    }

    @Override public void printTo(Printer printer) {
        printer.append("<").down().nextLine();

        printer.append("pid: ").append(row).append(",").nextLine();
        printer.append("name: ").append(name).append(",").nextLine();
        printer.append("t: ").append(complexProperty).append(",");

        printer.up().nextLine().append(">");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

}
