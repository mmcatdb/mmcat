/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.koupil
 */
public enum DocumentFactory {
	INSTANCE;

	public DocumentKind createKind() {
		return null;
	}

	public DocumentModel createModel() {
		return null;
	}

	public DocumentProperty createProperty(String name, Object value, boolean isIdentifierCompound, boolean isReferenceCompound, boolean isNullable) {
		return new DocumentProperty(name, value, isIdentifierCompound, isReferenceCompound, isNullable);
	}

	public DocumentRecord createRecord() {
		return new DocumentRecord();
	}

	public DocumentRecord createRecord(String name) {
		return new DocumentRecord(name);
	}

	public DocumentArray createArray(String name) {
		return new DocumentArray(name);
	}

	public DocumentPropertyValue createValue(String name, Object value) {
		return new DocumentPropertyValue(name, value);
	}
}
