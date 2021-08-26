package de.hda.fbi.modules.schemaextraction.common;

import java.io.Serializable;

public enum PropertyType implements Serializable {

    JsonObject, JsonArray, JsonNull, String, Number, Boolean
}
