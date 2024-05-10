/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.utils.BloomFilter;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.koupil
 */
public class ProcessedProperty implements Serializable, Comparable<ProcessedProperty> {

	private Set<ProcessedProperty> children;
	private ProcessedProperty parent;
	private RecordSchemaDescription schema;
	private PropertyHeuristics heuristics;
	private String hierarchicalName;

	public static ProcessedProperty empty() {
		return null;	// TODO:
	}

	public ProcessedProperty(String hierarchicalName, RecordSchemaDescription schema) {
		this.children = new TreeSet<>();
		this.hierarchicalName = hierarchicalName;
		this.schema = schema;
	}

	public ProcessedProperty(RawProperty rawProperty) {
		this.children = new TreeSet<>();
		this.hierarchicalName = rawProperty.getKey();
		if (rawProperty.getSchema() == null) {
			this.schema = new RecordSchemaDescription();
		} else {
			this.schema = rawProperty.getSchema();
			this.schema.setShareTotal(rawProperty.getCount());
			this.schema.setShareFirst(rawProperty.getFirst());
		}
// ----- REMOVE THE FOLLOWING CONTENT IN ORDER TO FIX PROPERTY_BASED ALGORITHM -----------------------------------------
		this.heuristics = new PropertyHeuristics() {
			{		// REMOVE COMMENT IN ORDER TO FIX CANDIDATE MINER ALGORITHM
				Object value = rawProperty.getValue();
				setHierarchicalName(rawProperty.getKey());
				setMin(value);
				setMax(value);
				if (value instanceof Number) {
					setTemp(((Number) value).doubleValue());
				} else if (value instanceof Comparable) {
					Double resultOfHashFunction = new BasicHashFunction().apply(value).doubleValue();

					setTemp(resultOfHashFunction);
				}
				setFirst(rawProperty.getFirst());
				setCount(rawProperty.getCount());
				setUnique(rawProperty.getCount() == 1);
				BloomFilter bloomFilter = new BloomFilter();
				if (value != null) {
					bloomFilter.add(value);
				}
                		setBloomFilter(bloomFilter);
			}
		};
// ---------------------------------------------------------------------------------------------------------------------
	}

	public void addChild(ProcessedProperty child) {
		this.children.add(child);
	}
	@Override
	public int compareTo(ProcessedProperty o) {
		return this.hierarchicalName.compareTo(o.hierarchicalName);
	}

	public RecordSchemaDescription getRSD() {
		return schema;
	}

	public Set<ProcessedProperty> getChildren() {
		return children;
	}
	public void setChildren(Set<ProcessedProperty> children) {
		this.children = children;
	}
	public ProcessedProperty getParent() {
		return parent;
	}

	public void setParent(ProcessedProperty parent) {
		this.parent = parent;
	}

	public RecordSchemaDescription getSchema() {
		return schema;
	}

	public void setSchema(RecordSchemaDescription schema) {
		this.schema = schema;
	}

	public PropertyHeuristics getHeuristics() {
		return heuristics;
	}

	public void setHeuristics(PropertyHeuristics heuristics) {
		this.heuristics = heuristics;
	}

	public String getHierarchicalName() {
		return hierarchicalName;
	}

	public void setHierarchicalName(String hierarchicalName) {
		this.hierarchicalName = hierarchicalName;
	}
}
