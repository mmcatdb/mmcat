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
public enum AbstractType {
	KIND,
	RECORD,
	ARRAY,	// pouze complex array, tedy pole poli nebo pole atributu!
	INLINED, // inlined verze structured attribute
	ATTRIBUTE,
	MULTI_ATTRIBUTE, // atribut s vetsi kardinalitou, tedy pole jednoduchych atributu
	STRUCTURED_ATTRIBUTE,
	IDENTIFIER,
	REFERENCE,
	MULTI_REFERENCE	// reference s vetsi kardinalitou

//	VALUE	// over, jestli je to spravne!
}
