/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.mminfer.model;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author pavel.koupil
 */
public class RawProperty implements Serializable, Comparable<RawProperty> {

	private String key;	// hierarchicalName

	private Object value;

	private RecordSchemaDescription schema;

	private Integer count; // repeat, kolikrat se tam ta hodnota nasla pres mapReduce ... pokud je ruzna od 1, tak neni unique!

	private Integer first;

	public RawProperty() {
	}

	public RawProperty(String key, Object value, RecordSchemaDescription schema, Integer count, Integer first) {
		this.key = key;
		this.value = value;
		this.schema = schema;
		this.count = count;
		this.first = first;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public RecordSchemaDescription getSchema() {
		return schema;
	}

	public void setSchema(RecordSchemaDescription schema) {
		this.schema = schema;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getFirst() {
		return first;
	}

	public void setFirst(Integer first) {
		this.first = first;
	}

	@Override
	public int compareTo(RawProperty o) {
		return this.key.compareTo(o.key);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RawProperty that = (RawProperty) o;
		return Objects.equals(key, that.key) && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RawProperty{");
		sb.append("key=").append(key);
		sb.append(", value=").append(value);
		sb.append(", schema=").append(schema);
		sb.append(", count=").append(count);
		sb.append(", first=").append(first);
		sb.append('}');
		return sb.toString();
	}

}
