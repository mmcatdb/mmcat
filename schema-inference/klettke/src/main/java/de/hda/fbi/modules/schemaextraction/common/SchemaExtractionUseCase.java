package de.hda.fbi.modules.schemaextraction.common;

public enum SchemaExtractionUseCase {
    AnalyseDatabase(1), Initial(2), Incremental(3);

    private int value;

    SchemaExtractionUseCase(int value) {
        this.setValue(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}