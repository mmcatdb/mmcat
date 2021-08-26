package de.hda.fbi.modules.schemaextraction.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class PropertyTypeTranslator {

    public PropertyType getPropertyType(JsonElement e) {

        if (e.isJsonObject()) {
            return PropertyType.JsonObject;
        } else if (e.isJsonArray()) {
            return PropertyType.JsonArray;
        } else if (e.isJsonNull()) {
            return PropertyType.JsonNull;
        } else {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isString()) {
                return PropertyType.String;
            } else if (p.isNumber()) {
                return PropertyType.Number;
            } else if (p.isBoolean()) {
                return PropertyType.Boolean;
            } else if (p.isJsonNull()) {
                return PropertyType.JsonNull;
            } else {
                return null;
            }
        }
    }
}
