package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * A path wrapper implementation for CSV files that implements the {@link AbstractPathWrapper} interface.
 * This class provides methods for managing and validating property paths within CSV data.
 */
public class CsvPathWrapper implements AbstractPathWrapper {

    private final List<String> properties = new ArrayList<>();

    /**
     * Adds a property path to the list of properties managed by this wrapper.
     *
     * @param path the property path to add.
     */
    @Override public void addProperty(String path) {
        properties.add(path);
    }

    /**
     * Checks the validity or existence of the paths added to this wrapper.
     * This method is currently not implemented.
     *
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override public boolean check() {
        throw new UnsupportedOperationException("CsvPathWrapper.check not implemented.");
    }


    // CHECKSTYLE:OFF
    // TODO check the correctness of the following methods:
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return true; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return true; }
    @Override public boolean isGroupingAllowed() { return true; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return true; }
    @Override public boolean isSchemaless() { return true; }
    // CHECKSTYLE:ON
}
