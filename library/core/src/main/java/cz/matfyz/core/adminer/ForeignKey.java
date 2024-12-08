package cz.matfyz.core.adminer;

/**
 * The ForeignKey class represents a foreign key relationship in a database.
 * It encapsulates information about the referencing table, column,
 * and the referenced table and column.
 */
public class ForeignKey {
    private String foreignTable;
    private String column;
    private String foreignColumn;

    public ForeignKey(String foreignTable, String column, String foreignColumn) {
        this.foreignTable = foreignTable;
        this.column = column;
        this.foreignColumn = foreignColumn;
    }

    public String getForeignTable() {
        return foreignTable;
    }

    public String getColumn() {
        return column;
    }

    public String getForeignColumn() {
        return foreignColumn;
    }

    @Override
    public String toString() {
        return String.format("""
                {
                    "column": "%s",
                    "foreign_column": "%s",
                    "foreign_table": "%s"
                }
                """, column, foreignColumn, foreignTable);
    }
}

