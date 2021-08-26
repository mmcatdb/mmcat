package de.hda.fbi.modules.schemaextraction.common;

import com.google.gson.JsonObject;

/***
 * Stellt eine gebündelte Dokumentinformation zur Verfügung
 *
 * @author Daniel
 *
 */
public class EntityInfo {

    private JsonObject entity;

    private Integer timestamp;

    private String entityName;

    private Object id;

    public EntityInfo(JsonObject entity, Integer timestamp, String entityName, Object id) {
        this.entity = entity;
        this.timestamp = timestamp;
        this.setId(id);
        this.setEntityName(entityName);
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public JsonObject getEntity() {
        return entity;
    }

    public void setEntity(JsonObject entity) {
        this.entity = entity;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }
}
