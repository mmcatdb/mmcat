package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Data Definition Language (DDL) wrapper for CSV that implements the {@link AbstractDDLWrapper} interface.
 * This class provides methods to define and manage CSV schema properties and create DDL statements for CSV data.
 */
public class CsvDDLWrapper implements AbstractDDLWrapper {

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public void clear() {
        kindName = null;
        properties.clear();
    }

    private String kindName = null;
    private final List<String> properties = new ArrayList<>();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        if (path.segments().size() != 1)
            throw InvalidPathException.wrongLength(DatasourceType.csv, path);

        final PathSegment segment = path.segments().get(0);
        // The postgres structure is flat.
        if (isComplex)
            throw InvalidPathException.isComplex(DatasourceType.csv, path);

        if (segment.isArray())
            throw InvalidPathException.isArray(DatasourceType.csv, path);

        properties.addAll(segment.names());
    }

    /**
     * Creates a DDL statement for the CSV schema by generating a header line with the
     * specified properties.
     */
    @Override public StringStatement createDDLStatement() {
        final String headerLine = String.join(",", properties);
        return StringStatement.create(headerLine);
    }

    @Override
    public Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands) {
        throw new UnsupportedOperationException("Unimplemented method 'createDDLDeleteStatements'");
    }

    @Override
    public AbstractStatement createCreationStatement(String newDBName, String owner) {
        throw new UnsupportedOperationException("Unimplemented method 'createCreationStatement'");
    }
}
