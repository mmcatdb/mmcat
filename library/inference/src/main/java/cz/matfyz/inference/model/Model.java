/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.inference.model;

/**
 *
 * @author pavel.koupil
 */
public abstract class Model {

	public static final int UNDEFINED = 0;
	public static final int DOC = (int) Math.pow(2, 0);
	public static final int REL = (int) Math.pow(2, 1);
	public static final int GRAPH = (int) Math.pow(2, 2);
	public static final int COL = (int) Math.pow(2, 3);
	public static final int KV = (int) Math.pow(2, 4);
	public static final int RDF = (int) Math.pow(2, 5);
	public static final int ARRAY = (int) Math.pow(2, 6);
}

//public enum Model {
//    DOC, REL, GRAPH, COL, KV, RDF, ARRAY, UNDEFINED;
//}
