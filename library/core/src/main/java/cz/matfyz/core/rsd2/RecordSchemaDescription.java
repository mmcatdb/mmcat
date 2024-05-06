/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
//import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList; 


/**
 *
 * @author pavel.koupil
 */
public final class RecordSchemaDescription implements Serializable, Comparable<RecordSchemaDescription> {

	private String name;

	private int unique;	// TODO: rozepsat na konstanty vestaveneho datoveho typu char, podobne jako typy a modely

	private int shareTotal;	// TODO: rozepsat na dve property, int total a int first

	private int shareFirst;

	private int id;		// TODO: rozepsat na konstanty vestaveneho datoveho typu char, podobne jako typy a modely

	private /*Set<Type>*/ int types;

	private /*Set<Model>*/ int models;

	private /*List*/ ObjectArrayList<RecordSchemaDescription> children;	// TODO: pouzit knihovnu https://trove4j.sourceforge.net/javadocs/gnu/trove/list/linked/TLinkedList.html nebo podobne efektivni a vhodnou - mene vytvorenych objektu, pametove uspornejsi a ve vysledku rychlejsi

	private RegExp regExp;

	private Reference ref;

	public RecordSchemaDescription() {
		this("", Char.UNKNOWN, 0, 0/*new Share()*/, Char.UNKNOWN, /*new TreeSet<>(), new TreeSet<>(),*/ new /*ArrayList*/ ObjectArrayList<>(), null, null);
	}

	public RecordSchemaDescription(
			String name,
			int unique,
			int shareTotal, int shareFirst, //Share share,
			int id,
			//			Set<Type> types,
			//			Set<Model> models,
			/*List*/ ObjectArrayList<RecordSchemaDescription> children,
			RegExp regExp,
			Reference ref) {
		this.name = name;
		this.unique = unique;
//		this.share = share;
		this.shareTotal = shareTotal;
		this.shareFirst = shareFirst;
		this.id = id;
		this.types = 0;
		this.models = 0;
		this.children = children;
//		this.regExp = regExp;
//		this.ref = ref;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUnique() {
		return unique;
	}

	public void setUnique(int unique) {
		this.unique = unique;
	}

	public int getShareTotal() {
		return shareTotal;
	}

	public void setShareTotal(int shareTotal) {
		this.shareTotal = shareTotal;
	}

	public int getShareFirst() {
		return shareFirst;
	}

	public void setShareFirst(int shareFirst) {
		this.shareFirst = shareFirst;
	}

//	public Share getShare() {
//		return share;
//	}
//
//	public void setShare(Share share) {
//		this.share = share;
//	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public /*Set<Type>*/ int getTypes() {
		return types;
	}

	public void setTypes(/*Set<Type>*/int types) {
		this.types = types;
	}

	public /*Set<Model>*/ int getModels() {
		return models;
	}

	public void setModels(/*Set<Model>*/int models) {
		this.models = models;
	}

	public /*List*/ ObjectArrayList<RecordSchemaDescription> getChildren() {
		return children;
	}

	public void setChildren(/*List*/ObjectArrayList<RecordSchemaDescription> children) {
		this.children = children;
	}

	public RegExp getRegExp() {
		return regExp;
	}

	public void setRegExp(RegExp regExp) {
		this.regExp = regExp;
	}

	public Reference getRef() {
		return ref;
	}

	public void setRef(Reference ref) {
		this.ref = ref;
	}

	@Override
	public int compareTo(RecordSchemaDescription o) {
		// WARN: TOHLE JE SPATNE, JE TU BUG! TAKHLE SE TO POROVNAVAT NEDA
		// A NAVIC JE TO PRASARNA
		int comparedNames = name.compareTo(o.name);
		boolean typesAreEqual = types == o.types;
//		boolean typesAreEqual = types.equals(o.types);
		if (comparedNames != 0) {
			return comparedNames;
		}

		return typesAreEqual ? 0 : -1;
	}

//	@Override
	public String _toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RecordSchemaDescription{");
		sb.append("name=").append(name);
		sb.append(", unique=").append(unique);
//		sb.append(", share=").append(share);
		sb.append(", shareTotal=").append(shareTotal);
		sb.append(", shareFirst=").append(shareFirst);
		sb.append(", id=").append(id);
		sb.append(", types=").append(types);
		sb.append(", models=").append(models);
		sb.append(", children=").append(children);
		sb.append(", regExp=").append(regExp);
		sb.append(", ref=").append(ref);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public String toString() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
//			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);	// pretty print
			objectMapper.disable(SerializationFeature.INDENT_OUTPUT);	// pretty print disabled

			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return _toString();// super.toString(); // Fallback to the default toString() if an exception occurs
		}
	}

}
