package cz.matfyz.core.utils.printable;

public interface Printer {

    Printer down();
    
    Printer up();
    
    Printer nextLine();
    
    Printer append(Printable printable);
    
    Printer append(String str);
    
    Printer append(int i);
    
    Printer append(Object obj);
    
    Printer remove();
    
    Printer remove(int i);

    /**
     * Utility method for providing a default printer.
     * @return
     */
    static Printer create() {
        return new LineStringBuilder(0);
    }

    /**
     * Utility method for printing a printable to a string. Should be used in the toString method of the printable.
     */
    static String print(Printable printable) {
        final var printer = create();
        printable.printTo(printer);
        return printer.toString();
    }

}
