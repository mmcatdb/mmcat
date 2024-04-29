package cz.matfyz.transformations.algorithms;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.utils.printable.*;

public class DMLStackTriple implements Printable {

    public final DomainRow pid;
    public final String name;
    public final ComplexProperty t;

    public DMLStackTriple(DomainRow pid, String name, ComplexProperty t) {
        this.pid = pid;
        this.name = name;
        this.t = t;
    }

    @Override public void printTo(Printer printer) {
        printer.append("<").down().nextLine();

        printer.append("pid: ").append(pid).append(",").nextLine();
        printer.append("name: ").append(name).append(",").nextLine();
        printer.append("t: ").append(t).append(",");

        printer.up().nextLine().append(">");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

}
