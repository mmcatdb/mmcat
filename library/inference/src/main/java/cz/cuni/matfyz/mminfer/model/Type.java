/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.cuni.matfyz.mminfer.model;

/**
 *
 * @author pavel.koupil
 */
public abstract class Type {

	public static final int UNKNOWN = 0;
	public static final int OBJECT = (int) Math.pow(2, 0); // 1
	public static final int STRING = (int) Math.pow(2, 1); // 2
	public static final int BOOLEAN = (int) Math.pow(2, 2); // 4
	public static final int NUMBER = (int) Math.pow(2, 3); // 8
	public static final int ARRAY = (int) Math.pow(2, 4); // 16
	public static final int MAP = (int) Math.pow(2, 5); // 32
	public static final int DATE = (int) Math.pow(2, 6); //64

}

//public enum Type {
//    // TODO: DOKONCI! NENI TU VSECHNO!
//    OBJECT(1), STRING(1, 2), BOOLEAN(1, 2, 3), NUMBER(1, 2, 5), ARRAY(1, 2, 7), MAP(1, 2, 11), DATE(1, 2, 13);
//
//    private Type(int... hierarchy) {
//        // TODO, according to algebra of types
//    }
//
//    // TODO: operace GCD a podobne, according to algebra of types
//}
