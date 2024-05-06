/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

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

	private int count; // TODO: primitive type!

	private int first;	// TODO: primitive type!

	public RawProperty() {
	}

	public RawProperty(String key, Object value, RecordSchemaDescription schema, int count, int first) {
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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
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
