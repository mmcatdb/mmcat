package de.hda.fbi.modules.schemaextraction.common;

public class DataTypeTranslator {

    public static DataType translate(PropertyType propertyType) {
        switch (propertyType) {
            case Boolean:
                return DataType.BOOLEAN;
            case String:
                return DataType.STRING;
            case Number:
                return DataType.INTEGER;
        }
        return null;
    }
}