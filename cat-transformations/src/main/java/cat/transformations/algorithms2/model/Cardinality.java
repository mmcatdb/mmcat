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
public enum Cardinality {
	ONE_TO_ONE,
	ONE_TO_MANY, MANY_TO_ONE,
	MANY_TO_MANY,
	ZERO_TO_ONE, ONE_TO_ZERO,
	ZERO_TO_MANY, MANY_TO_ZERO;

}
